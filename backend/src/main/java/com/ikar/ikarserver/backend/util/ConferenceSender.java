package com.ikar.ikarserver.backend.util;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;

import java.io.IOException;

public final class ConferenceSender {

    public static void sendViewerRegisterSuccess(ConferenceUserSession userSession) throws IOException {
        JsonObject message = new JsonObject();
        message.addProperty("id", "viewerRegistered");

        userSession.sendMessage(message);
    }


}
