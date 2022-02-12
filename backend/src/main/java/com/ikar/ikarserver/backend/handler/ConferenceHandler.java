package com.ikar.ikarserver.backend.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.conference.Conference;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceManager;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserRegistry;
import com.ikar.ikarserver.backend.exception.app.AppException;
import com.ikar.ikarserver.backend.handler.message.conference.ConferenceMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ConferenceHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();

    private final ConferenceUserRegistry userRegistry;
    private final ConferenceManager conferenceManager;
    private final Map<String, ConferenceMessageHandler> handlers;

    public ConferenceHandler(ConferenceUserRegistry userRegistry, ConferenceManager conferenceManager, List<ConferenceMessageHandler> handlers) {
        this.userRegistry = userRegistry;
        this.conferenceManager = conferenceManager;
        this.handlers = handlers
                .stream()
                .collect(Collectors.toMap(ConferenceMessageHandler::getProcessedMessage, ConferenceMessageHandler.class::cast));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        log.debug("Incoming message from session '{}': {}", session.getId(), jsonMessage);

        try {
            final String messageId = jsonMessage.get("id").getAsString();
            ConferenceMessageHandler handler = handlers.get(messageId);
            handler.process(jsonMessage, session);
        } catch (AppException e) {
            handleErrorResponse(e.getMessage(), session);
        } catch (Exception e) {
            log.error(e.getMessage());
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

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Optional<Conference> opt = userRegistry.getConferenceBySession(session);
        if (opt.isPresent()) {
            opt.get().leave(session);
        }
        userRegistry.removeBySession(session);
    }

}
