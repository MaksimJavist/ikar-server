package com.ikar.ikarserver.backend.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConference;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConferenceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewConferenceHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();

    private final NewConferenceManager conferenceManager;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        log.debug("Incoming message from session '{}': {}", session.getId(), jsonMessage);

        switch (jsonMessage.get("id").getAsString()) {
            case "registerViewer": {
                NewConference conference = getConferenceByIdentifier(jsonMessage.get("conference").getAsString());
                conference.registerViewer(session);
            }
            case "presenter":
                try {
                    NewConference conference = getConferenceByIdentifier(jsonMessage.get("conference").getAsString());
                    conference.presenter(session, jsonMessage);
                } catch (Throwable t) {
                    handleErrorResponse(t, session, "presenterResponse");
                }
                break;
            case "viewer":
                try {
                    NewConference conference = getConferenceByIdentifier(jsonMessage.get("conference").getAsString());
                    conference.viewer(session, jsonMessage);
                } catch (Throwable t) {
                    handleErrorResponse(t, session, "viewerResponse");
                }
                break;
            case "onIceCandidate": {
                NewConference conference = getConferenceByIdentifier(jsonMessage.get("conference").getAsString());
                conference.addIceCandidate(jsonMessage, session);
                break;
            }
            case "stop": {
                NewConference conference = getConferenceByIdentifier(jsonMessage.get("conference").getAsString());
                conference.stop(session);
                break;
            }
            default:
                break;
        }
    }

    private void handleErrorResponse(Throwable throwable, WebSocketSession session, String responseId)
            throws IOException {
//        stop(session);
//        log.error(throwable.getMessage(), throwable);
//        JsonObject response = new JsonObject();
//        response.addProperty("id", responseId);
//        response.addProperty("response", "rejected");
//        response.addProperty("message", throwable.getMessage());
//        session.sendMessage(new TextMessage(response.toString()));
    }

    private NewConference getConferenceByIdentifier(String identifier) {
        return conferenceManager.getConference(identifier).get();
    }

//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        stop(session);
//    }

}
