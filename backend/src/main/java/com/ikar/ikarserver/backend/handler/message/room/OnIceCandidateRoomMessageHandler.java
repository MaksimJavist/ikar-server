package com.ikar.ikarserver.backend.handler.message.room;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import lombok.RequiredArgsConstructor;
import org.kurento.client.IceCandidate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class OnIceCandidateRoomMessageHandler implements RoomMessageHandler {

    private final RoomUserRegistry registry;

    @Override
    public void process(JsonObject message, WebSocketSession session) {
        final RoomUserSession user = registry.getBySession(session);

        JsonObject candidateMessage = message.get("candidate").getAsJsonObject();

        if (user != null) {
            IceCandidate candidate = new IceCandidate(
                    candidateMessage.get("candidate").getAsString(),
                    candidateMessage.get("sdpMid").getAsString(),
                    candidateMessage.get("sdpMLineIndex").getAsInt()
            );
            user.addCandidate(candidate, message.get("uuid").getAsString());
        }
    }

    @Override
    public String getProcessedMessage() {
        return "onIceCandidate";
    }
}
