package com.ikar.ikarserver.backend.handler.message.room;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.Room;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomManager;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PresenterStopCommunicationRoomMessageHandler implements RoomMessageHandler {

    private final RoomManager roomManager;
    private final RoomUserRegistry registry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        RoomUserSession user = registry.getBySession(session);
        Room room = roomManager.getRoom(user.getRoomUuid());
        room.presenterStopCommunication(user);
    }

    @Override
    public String getProcessedMessage() {
        return "presenterStopCommunication";
    }
}
