package com.ikar.ikarserver.backend.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomManager;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import com.ikar.ikarserver.backend.handler.message.room.RoomMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.kurento.commons.exception.KurentoException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RoomHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();

    private final RoomUserRegistry registry;
    private final RoomManager manager;
    private final Map<String, RoomMessageHandler> handlers;

    public RoomHandler(RoomUserRegistry registry, RoomManager manager, List<RoomMessageHandler> handlers) {
        this.registry = registry;
        this.manager = manager;
        this.handlers = handlers
                .stream()
                .collect(Collectors.toMap(RoomMessageHandler::getProcessedMessage, RoomMessageHandler.class::cast));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

        try {
            final String messageId = jsonMessage.get("id").getAsString();
            RoomMessageHandler handler = handlers.get(messageId);
            handler.process(jsonMessage, session);
        } catch (KurentoException ke) {
            log.error(ke.getMessage());
        } catch (Exception e) {
            handleErrorResponse(e.getMessage(), session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        RoomUserSession user = registry.removeBySession(session);
        manager.getRoom(user.getRoomUuid()).leave(user);
    }

    private void handleErrorResponse(String message, WebSocketSession session) throws IOException {
        log.error(message);
        JsonObject response = new JsonObject();
        response.addProperty("id", "errorResponse");
        response.addProperty("message", message);
        session.sendMessage(new TextMessage(response.toString()));
    }

}
