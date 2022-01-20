package com.ikar.ikarserver.backend.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConference;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.newconference.NewConferenceUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.newconference.UserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import com.ikar.ikarserver.backend.exception.websocket.ConferenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_NOT_FOUND;
import static com.ikar.ikarserver.backend.util.Messages.CONFERENCE_USER_NOT_EXIST;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewConferenceHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();

    private final NewConferenceManager conferenceManager;
    private final NewConferenceUserRegistry userRegistry;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        log.debug("Incoming message from session '{}': {}", session.getId(), jsonMessage);

        try {
            switch (jsonMessage.get("id").getAsString()) {
                case "registerViewer": {
                    NewConference conference = getConferenceByIdentifier(jsonMessage.get("conference").getAsString());
                    String name = jsonMessage.get("name").getAsString();
                    UserSession registeredUser = conference.registerViewer(session, name);
                    userRegistry.register(registeredUser, conference);
                    break;
                }
                case "viewerConnectPermission": {
                    NewConference conference = getConference(session);
                    conference.viewerConnectPermission(session);
                    break;
                }
                case "viewer": {
                    NewConference conference = getConference(session);
                    conference.viewer(session, jsonMessage);
                    break;
                }
                case "presenterConnectPermission": {
                    NewConference conference = getConference(session);
                    conference.presenterConnectPermission(session);
                    break;
                }
                case "presenter": {
                    NewConference conference = getConference(session);
                    conference.presenter(session, jsonMessage);
                    break;
                }
                case "onIceCandidate": {
                    NewConference conference = getConference(session);
                    conference.addIceCandidate(jsonMessage, session);
                    break;
                }
                case "sendChat": {
                    UserSession user = getUserBySession(session);
                    NewConference conference = getConference(session);
                    String chatMessage = jsonMessage.get("message").getAsString();
                    conference.sendNewMessage(
                            new ChatMessageDto(
                                    user.getUuid(),
                                    user.getUsername(),
                                    LocalDateTime.now(),
                                    chatMessage
                            )
                    );
                    break;
                }
                case "stop": {
                    NewConference conference = getConference(session);
                    conference.stop(session);
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

    private UserSession getUserBySession(WebSocketSession session) throws ConferenceException {
        return userRegistry.getBySession(session)
                .orElseThrow(ConferenceException.supplier(CONFERENCE_USER_NOT_EXIST));
    }

    private NewConference getConferenceByIdentifier(String identifier) throws ConferenceException {
        return conferenceManager.getConference(identifier)
                .orElseThrow(ConferenceException.supplier(CONFERENCE_NOT_FOUND));
    }

    private NewConference getConference(WebSocketSession session) throws ConferenceException {
        return userRegistry.getConferenceBySession(session)
                .orElseThrow(ConferenceException.supplier(CONFERENCE_NOT_FOUND));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Optional<NewConference> opt = userRegistry.getConferenceBySession(session);
        if (opt.isPresent()) {
            opt.get().leave(session);
        }
        userRegistry.removeBySession(session);
    }

}
