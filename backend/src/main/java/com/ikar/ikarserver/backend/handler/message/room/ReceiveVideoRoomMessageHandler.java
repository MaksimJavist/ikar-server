package com.ikar.ikarserver.backend.handler.message.room;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ReceiveVideoRoomMessageHandler implements RoomMessageHandler {

    private final RoomUserRegistry registry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        final RoomUserSession user = registry.getBySession(session);

        final String senderUuid = message.get("uuid").getAsString();
        final RoomUserSession sender = registry.getBySessionAndUuid(senderUuid, session);
        final String sdpOffer = message.get("sdpOffer").getAsString();
        user.receiveVideoFrom(sender, sdpOffer);
    }

    @Override
    public String getProcessedMessage() {
        return "receiveVideoFrom";
    }

}
