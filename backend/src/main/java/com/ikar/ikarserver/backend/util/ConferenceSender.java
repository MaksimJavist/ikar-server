package com.ikar.ikarserver.backend.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NEW_USER_JOIN;
import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_USER_LEAVE;
import static com.ikar.ikarserver.backend.util.Messages.NOT_ACTIVE_PRESENTER;
import static com.ikar.ikarserver.backend.util.Messages.PRESENTER_BUSY;
import static com.ikar.ikarserver.backend.util.Messages.USER_IS_BROADCASTING;
import static com.ikar.ikarserver.backend.util.Messages.USER_STOP_PRESENTATION;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConferenceSender {

    public static void sendViewerRegisterSuccess(ConferenceUserSession userSession,
                                                 JsonArray conferenceUsersNames,
                                                 JsonArray chatMessages) throws IOException {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "viewerRegistered");
        message.addProperty("uuid", userSession.getUuid());
        message.addProperty("username", userSession.getUsername());
        message.add("users", conferenceUsersNames);
        message.add("messages", chatMessages);

        userSession.sendMessage(message);
    }

    public static void sendNewUserJoinForAll(Collection<ConferenceUserSession> notifiedUsers,
                                             ConferenceUserSession newUser) {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "newUserJoin");
        message.addProperty("uuid", newUser.getUuid());
        message.addProperty("name", newUser.getUsername());
        message.addProperty("message", MessageFormat.format(CONFERENCE_NEW_USER_JOIN, newUser.getUsername()));

        sendMessageForAllUsers(message, notifiedUsers);
    }

    public static void sendAcceptViewerConnectPermissionResponse(ConferenceUserSession session, String presenterName) throws IOException {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "viewerConnectPermissionResponse");
        message.addProperty("response", "accepted");
        message.addProperty("message", MessageFormat.format(USER_IS_BROADCASTING, presenterName));

        session.sendMessage(message);
    }

    public static void sendRejectViewerConnectPermissionResponse(ConferenceUserSession session) throws IOException {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "viewerConnectPermissionResponse");
        message.addProperty("response", "reject");
        message.addProperty("message", NOT_ACTIVE_PRESENTER);

        session.sendMessage(message);
    }

    public static void sendAcceptPresenterConnectPermissionResponse(ConferenceUserSession session) throws IOException {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "presenterConnectPermissionResponse");
        message.addProperty("response", "accepted");

        session.sendMessage(message);
    }

    public static void sendRejectPresenterConnectPermissionResponse(ConferenceUserSession session) throws IOException {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "presenterConnectPermissionResponse");
        message.addProperty("response", "reject");
        message.addProperty("message", PRESENTER_BUSY);

        session.sendMessage(message);
    }

    public static void sendRejectPresenterResponse(ConferenceUserSession session) throws IOException {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "presenterResponse");
        message.addProperty("response", "rejected");
        message.addProperty("message", PRESENTER_BUSY);
        session.sendMessage(message);
    }

    public static void sendNewPresenterForAllViewers(Collection<ConferenceUserSession> viewers, ConferenceUserSession user) throws IOException {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "newPresenter");
        message.addProperty("presenterUuid", user.getUuid());
        message.addProperty("message", "Пользователь " + user.getUsername() + " начал трансляцию");

        sendMessageForAllUsers(message, viewers);
    }

    public static void sendIceCandidate(ConferenceUserSession user, IceCandidateFoundEvent event) throws IOException {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "iceCandidate");
        message.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));

        user.sendMessage(message);
    }

    public static void sendPresenterResponseSdpAnswer(ConferenceUserSession presenter, String sdpAnswer) throws IOException {
        final JsonObject response = new JsonObject();
        response.addProperty("id", "presenterResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", sdpAnswer);

        presenter.sendMessage(response);
    }

    public static void sendViewerResponseSdpAnswer(ConferenceUserSession viewer, String presenterName, String sdpAnswer) throws IOException {
        final JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "accepted");
        response.addProperty("message", presenterName + " ведет трансляцию");
        response.addProperty("sdpAnswer", sdpAnswer);

        viewer.sendMessage(response);
    }

    public static void sendRejectViewerResponse(WebSocketSession session, String message) throws IOException {
        final JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "rejected");
        response.addProperty("message", message);
        session.sendMessage(
                new TextMessage(
                        response.toString()
                )
        );
    }

    public static void sendPresenterStopForAllViewers(String presenterName, Collection<ConferenceUserSession> viewers) {
        final JsonObject response = new JsonObject();
        response.addProperty("id", "stopCommunication");
        response.addProperty("message", MessageFormat.format(USER_STOP_PRESENTATION, presenterName));

        sendMessageForAllUsers(response, viewers);
    }

    public static void sendUserLeaveFromConferenceForAllUsers(ConferenceUserSession leavedUser,
                                                              Collection<ConferenceUserSession> allUsers) {
        final JsonObject message = new JsonObject();
        message.addProperty("id", "userLeave");
        message.addProperty("uuid", leavedUser.getUuid());
        message.addProperty("message", MessageFormat.format(CONFERENCE_USER_LEAVE, leavedUser.getUsername()));

        sendMessageForAllUsers(message, allUsers);
    }

    public static void sendAllUsersNewChatMessage(ChatMessageDto chatMessageDto, List<ConferenceUserSession> users) {
        final JsonObject dataMessage = ChatMessageUtil.convertMessageToJson(chatMessageDto);
        final JsonObject response = new JsonObject();
        response.addProperty("id", "newChatMessage");
        response.add("data", dataMessage);

        sendMessageForAllUsers(response, users);
    }

    private static void sendMessageForAllUsers(JsonObject message, Collection<ConferenceUserSession> users) {
        for (ConferenceUserSession user : users) {
            try {
                user.sendMessage(message);
            } catch (IOException e) {
                log.debug("ROOM: participant {} could not be notified", user.getUsername(), e);
            }
        }
    }

}
