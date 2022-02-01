package com.ikar.ikarserver.backend.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceMessageBuffer;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import static com.ikar.ikarserver.backend.util.Messages.NOT_ACTIVE_PRESENTER;
import static com.ikar.ikarserver.backend.util.Messages.PRESENTER_BUSY;
import static com.ikar.ikarserver.backend.util.Messages.USER_IS_BROADCASTING;
import static com.ikar.ikarserver.backend.util.Messages.USER_STOP_PRESENTATION;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConferenceSender {

    public static void sendViewerRegisterSuccess(ConferenceUserSession userSession) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerRegistered");
        userSession.sendMessage(message);
    }

    public static void sendViewerRegisterSuccess(ConferenceUserSession userSession, JsonArray chatMessages) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerRegistered");
        message.addProperty("uuid", userSession.getUuid());
        message.addProperty("username", userSession.getUsername());
        message.add("messages", chatMessages);
        userSession.sendMessage(message);
    }

    public static void sendAcceptViewerConnectPermissionResponse(ConferenceUserSession session, String presenterName) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerConnectPermissionResponse");
        message.addProperty("response", "accepted");
        message.addProperty("message", MessageFormat.format(USER_IS_BROADCASTING, presenterName));
        session.sendMessage(message);
    }

    public static void sendRejectViewerConnectPermissionResponse(ConferenceUserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerConnectPermissionResponse");
        message.addProperty("response", "reject");
        message.addProperty("message", NOT_ACTIVE_PRESENTER);

        session.sendMessage(message);
    }

    public static void sendAcceptPresenterConnectPermissionResponse(ConferenceUserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "presenterConnectPermissionResponse");
        message.addProperty("response", "accepted");

        session.sendMessage(message);
    }

    public static void sendRejectPresenterConnectPermissionResponse(ConferenceUserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "presenterConnectPermissionResponse");
        message.addProperty("response", "reject");
        message.addProperty("message", PRESENTER_BUSY);

        session.sendMessage(message);
    }

    public static void sendRejectPresenterResponse(ConferenceUserSession session) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "presenterResponse");
        message.addProperty("response", "rejected");
        message.addProperty("message", PRESENTER_BUSY);
        session.sendMessage(message);
    }

    public static void sendNewPresenterForAllViewers(Collection<ConferenceUserSession> viewers, String presenterName) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "newPresenter");
        message.addProperty("message", "Пользователь " + presenterName + " начал трансляцию");
        for (ConferenceUserSession viewer : viewers) {
            viewer.sendMessage(message);
        }
    }

    public static void sendPresenterResponseSdpAnswer(ConferenceUserSession presenter, String sdpAnswer) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "presenterResponse");
        response.addProperty("response", "accepted");
        response.addProperty("sdpAnswer", sdpAnswer);
        presenter.sendMessage(response);
    }

    public static void sendViewerResponseSdpAnswer(ConferenceUserSession viewer, String presenterName, String sdpAnswer) throws IOException {
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

    public static void sendPresenterStopForAllViewers(String presenterName, Collection<ConferenceUserSession> viewers) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("id", "stopCommunication");
        response.addProperty("message", MessageFormat.format(USER_STOP_PRESENTATION, presenterName));

        sendMessageForAllUsers(response, viewers);
    }

    public static void sendAllUsersNewChatMessage(ChatMessageDto chatMessageDto, List<ConferenceUserSession> users) throws IOException {
        final JsonObject dataMessage = ConferenceMessageBuffer.convertChatMessageToJson(chatMessageDto);
        final JsonObject response = new JsonObject();
        response.addProperty("id", "newChatMessage");
        response.add("data", dataMessage);

        sendMessageForAllUsers(response, users);
    }

    private static void sendMessageForAllUsers(JsonObject message, Collection<ConferenceUserSession> users) throws IOException {
        for (ConferenceUserSession user : users) {
            user.sendMessage(message);
        }
    }

}
