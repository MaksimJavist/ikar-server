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
import java.util.Optional;

import static com.ikar.ikarserver.backend.util.Messages.CALL_USER_NOT_EXIST;

@Component
@RequiredArgsConstructor
public class ReceiveVideoRoomMessageHandler implements RoomMessageHandler {

    private final RoomUserRegistry registry;
    private final RoomManager roomManager;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        final RoomUserSession user = registry.getBySession(session)
                .orElseThrow(RoomException.supplier(CALL_USER_NOT_EXIST));

        final String senderUuid = message.get("uuid").getAsString();
        final RoomUserSession sender = getSessionAndUuid(user, senderUuid)
                .orElseThrow(RoomException.supplier(CALL_USER_NOT_EXIST));
        final String sdpOffer = message.get("sdpOffer").getAsString();
        user.receiveVideoFrom(sender, sdpOffer);
    }

    private Optional<RoomUserSession> getSessionAndUuid(RoomUserSession user, String senderUuid) {
        String roomIdentifier = user.getRoomUuid();
        final Room room = roomManager.getRoom(roomIdentifier);
        return Optional.ofNullable(
                room.getParticipant(senderUuid)
        );
    }

    @Override
    public String getProcessedMessage() {
        return "receiveVideoFrom";
    }

}
