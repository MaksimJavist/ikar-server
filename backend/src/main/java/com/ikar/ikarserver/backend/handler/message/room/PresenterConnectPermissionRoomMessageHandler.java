package com.ikar.ikarserver.backend.handler.message.room;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.Room;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomManager;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import com.ikar.ikarserver.backend.exception.websocket.RoomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.ikar.ikarserver.backend.util.Messages.CALL_USER_NOT_EXIST;

@Component
@RequiredArgsConstructor
public class PresenterConnectPermissionRoomMessageHandler implements RoomMessageHandler {

    private final RoomManager roomManager;
    private final RoomUserRegistry registry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        RoomUserSession user = registry.getBySession(session)
                .orElseThrow(RoomException.supplier(CALL_USER_NOT_EXIST));
        Room room = roomManager.getRoom(user.getRoomUuid());
        room.presenterConnectPermission(user);
    }

    @Override
    public String getProcessedMessage() {
        return "presenterConnectPermission";
    }
}
