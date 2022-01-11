package com.ikar.ikarserver.backend.util;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
import com.ikar.ikarserver.backend.domain.kurento.newconference.UserSession;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Collection;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NOT_ACTIVE_PRESENTER;
import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_PRESENTER_BUSY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConferenceSender {

    public static void sendViewerRegisterSuccess(ConferenceUserSession userSession) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerRegistered");
        userSession.sendMessage(message);
    }

    public static void sendViewerRegisterSuccess(UserSession userSession) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerRegistered");
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


}
