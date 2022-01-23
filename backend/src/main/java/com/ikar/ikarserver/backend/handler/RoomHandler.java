package com.ikar.ikarserver.backend.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.Room;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomManager;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import com.ikar.ikarserver.backend.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomHandler extends TextWebSocketHandler {

    private static final Gson gson = new GsonBuilder().create();

    private final RoomManager roomManager;
    private final RoomUserRegistry registry;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        final RoomUserSession user = registry.getBySession(session);

        try {
            switch (jsonMessage.get("id").getAsString()) {
                case "joinRoom":
                    joinRoom(jsonMessage, session);
                    break;
                case "receiveVideoFrom":
                    final String senderUuid = jsonMessage.get("uuid").getAsString();
                    final RoomUserSession sender = registry.getBySessionAndUuid(senderUuid, session);
                    final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
                    user.receiveVideoFrom(sender, sdpOffer);
                    break;
                case "leaveRoom":
                    leaveRoom(user);
                    break;
                case "onIceCandidate":
                    JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

                    if (user != null) {
                        IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
                                candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
                        user.addCandidate(cand, jsonMessage.get("uuid").getAsString());
                    }
                    break;
                case "sendChat":
                    RoomUserSession messageSender = registry.getBySession(session);
                    Room room = roomManager.getRoom(messageSender.getRoomUuid());
                    String chatMessage = jsonMessage.get("message").getAsString();
                    room.sendNewMessage(
                            new ChatMessageDto(
                                    messageSender.getUuid(),
                                    messageSender.getName(),
                                    LocalDateTime.now(),
                                    chatMessage
                            )
                    );
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            handleErrorResponse(e.getMessage(), session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        RoomUserSession user = registry.removeBySession(session);
        roomManager.getRoom(user.getRoomUuid()).leave(user);
    }

    private void handleErrorResponse(String message, WebSocketSession session) throws IOException {
        log.error(message);
        JsonObject response = new JsonObject();
        response.addProperty("id", "errorResponse");
        response.addProperty("message", message);
        session.sendMessage(new TextMessage(response.toString()));
    }

    private void joinRoom(JsonObject params, WebSocketSession session) throws IOException {
        final String roomName = params.get("room").getAsString();
        final String name = params.get("name").getAsString();
        log.info("PARTICIPANT {}: trying to join room {}", name, roomName);

        Room room = roomManager.getRoom(roomName);
        final RoomUserSession user = room.join(name, session);
        registry.register(user, room);
    }

    private void leaveRoom(RoomUserSession user) throws IOException {
        final Room room = roomManager.getRoom(user.getRoomUuid());
        room.leave(user);
        if (room.getParticipants().isEmpty()) {
            roomManager.removeRoom(room);
        }
    }

}
