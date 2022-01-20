package com.ikar.ikarserver.backend.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
import com.ikar.ikarserver.backend.domain.kurento.newconference.ConferenceMessageBuffer;
import com.ikar.ikarserver.backend.domain.kurento.newconference.UserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NOT_ACTIVE_PRESENTER;
import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_PRESENTER_BUSY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConferenceSender {

    public static void sendViewerRegisterSuccess(ConferenceUserSession userSession) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerRegistered");
        userSession.sendMessage(message);
    }

    public static void sendViewerRegisterSuccess(UserSession userSession, JsonArray chatMessages) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerRegistered");
        message.addProperty("uuid", userSession.getUuid());
        message.addProperty("username", userSession.getUsername());
        message.add("messages", chatMessages);
        userSession.sendMessage(message);
    }

    public static void sendAcceptViewerConnectPermissionResponse(UserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerConnectPermissionResponse");
        message.addProperty("response", "accepted");
        session.sendMessage(message);
    }

    public static void sendRejectViewerConnectPermissionResponse(UserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerConnectPermissionResponse");
        message.addProperty("response", "reject");
        message.addProperty("message", CONFERENCE_NOT_ACTIVE_PRESENTER);

        session.sendMessage(message);
    }

    public static void sendAcceptPresenterConnectPermissionResponse(UserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "presenterConnectPermissionResponse");
        message.addProperty("response", "accepted");

        session.sendMessage(message);
    }

    public static void sendRejectPresenterConnectPermissionResponse(UserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "presenterConnectPermissionResponse");
        message.addProperty("response", "reject");
        message.addProperty("message", CONFERENCE_PRESENTER_BUSY);

        session.sendMessage(message);
    }

    public static void sendRejectPresenterResponse(UserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "presenterResponse");
        message.addProperty("response", "rejected");
        message.addProperty("message", CONFERENCE_PRESENTER_BUSY);
        session.sendMessage(message);
    }

    public static void sendNewPresenterForAllViewers(Collection<UserSession> viewers, String presenterName) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "newPresenter");
        message.addProperty("message", "Пользователь " + presenterName + " начал трансляцию");
        for (UserSession viewer : viewers) {
            viewer.sendMessage(message);
        }
    }

    public static void sendPresenterResponseSdpAnswer(UserSession presenter, String sdpAnswer) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "presenterResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", sdpAnswer);
        presenter.sendMessage(response);
    }

    public static void sendViewerResponseSdpAnswer(UserSession viewer, String presenterName, String sdpAnswer) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "accepted");
        response.addProperty("message", presenterName + " ведет трансляцию");
        response.addProperty("sdpAnswer", sdpAnswer);
        viewer.sendMessage(response);
    }

    public static void sendRejectViewerResponse(WebSocketSession session, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "viewerResponse");
        response.addProperty("response", "rejected");
        response.addProperty("message", message);
        session.sendMessage(
                new TextMessage(
                        response.toString()
                )
        );
    }

    public static void sendPresenterLeaveForViewer(UserSession viewer, String presenterName) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "presenterLeave");
        response.addProperty("message", "Презентующий " + presenterName + " покинул трансляцию.");
        viewer.sendMessage(response);
    }

    public static void sendPresenterStopForViewer(UserSession viewer, String presenterName) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "stopCommunication");
        response.addProperty("message", "Пользователь " + presenterName + " прекратил трансляцию.");
        viewer.sendMessage(response);
    }

    public static void sendAllUsersNewChatMessage(ChatMessageDto chatMessageDto, List<UserSession> users) throws IOException {
        final JsonObject dataMessage = ConferenceMessageBuffer.convertChatMessageToJson(chatMessageDto);
        final JsonObject response = new JsonObject();
        response.addProperty("id", "newChatMessage");
        response.add("data", dataMessage);

        for (UserSession user : users) {
            user.sendMessage(response);
        }
    }

}
