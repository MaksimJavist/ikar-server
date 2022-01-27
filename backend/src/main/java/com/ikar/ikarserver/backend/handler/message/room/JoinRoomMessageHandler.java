package com.ikar.ikarserver.backend.handler.message.room;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.Room;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomManager;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinRoomMessageHandler implements RoomMessageHandler {

    private final RoomManager roomManager;
    private final RoomUserRegistry registry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        final String roomIdentifier = message.get("room").getAsString();
        final String name = message.get("name").getAsString();
        log.info("PARTICIPANT {}: trying to join room {}", name, roomIdentifier);
        Room room = roomManager.getRoom(roomIdentifier);
        final RoomUserSession user = room.join(name, session);
        registry.register(user, room);
    }

    @Override
    public String getProcessedMessage() {
        return "joinRoom";
    }
}
