package com.ikar.ikarserver.backend.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.Conference;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import com.ikar.ikarserver.backend.util.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NOT_FOUND;
import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_USER_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConferenceHandler extends TextWebSocketHandler {

    private final ConferenceManager conferenceManager;
    private final ConferenceUserRegistry userRegistry;

    private final Gson gson = new GsonBuilder().create();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        log.debug("Incoming message from session '{}': {}", session.getId(), jsonMessage);

        try {
            switch (jsonMessage.get("id").getAsString()) {
                case "registerViewer": {
                    String name = jsonMessage.get("name").getAsString();
                    String conferenceIdentifier = jsonMessage.get("conference").getAsString();
                    Conference conference = getConferenceByIdentifier(conferenceIdentifier);
                    ConferenceUserSession user = conference.registerViewer(session, name);
                    userRegistry.register(user, conference);
                    log.info("VIEWER: trying to join conference {}", conference.getIdentifier());
                    break;
                }
                case "presenter": {
                    Conference conference = getConferenceBySession(session);
                    conference.presenter(session, jsonMessage);
                    break;
                }
                case "viewer": {
                    Conference conference = getConferenceBySession(session);
                    conference.viewer(session, jsonMessage);
                    break;
                }
                case "onIceCandidate": {
                    Conference conference = getConferenceBySession(session);
                    JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
                    conference.onIceCandidate(session, candidate);
                    break;
                }
                case "stopCommunication": {
                    Conference conference = getConferenceBySession(session);
                    conference.stopCommunication(session);
                }
                case "leave": {
                    Conference conference = getConferenceBySession(session);
                    conference.leave(session);
                    break;
                }
                default:
                    break;
            }
        } catch (Exception e) {
            handleErrorResponse(e.getMessage(), session);
        }

    }

    private void handleErrorResponse(String message, WebSocketSession session)
            throws IOException {
        log.error(message);
        JsonObject response = new JsonObject();
        response.addProperty("id", "errorResponse");
        response.addProperty("message", message);
        session.sendMessage(new TextMessage(response.toString()));
    }

    private Conference getConferenceBySession(WebSocketSession session) throws ConferenceException {
        ConferenceUserSession user =
                userRegistry.getBySession(session)
                        .orElseThrow(ConferenceException.supplier(CONFERENCE_USER_NOT_FOUND));
        return conferenceManager.getConference(user.getConferenceUuid())
                .orElseThrow(ConferenceException.supplier(CONFERENCE_NOT_FOUND));
    }

    private Conference getConferenceByIdentifier(String identifier) throws ConferenceException {
        return conferenceManager.getConference(identifier)
                .orElseThrow(ConferenceException.supplier(CONFERENCE_NOT_FOUND));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Conference conference = getConferenceBySession(session);
        conference.leave(session);
        userRegistry.removeBySession(session);
    }
}