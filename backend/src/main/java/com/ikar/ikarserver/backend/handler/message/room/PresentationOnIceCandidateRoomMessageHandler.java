package com.ikar.ikarserver.backend.handler.message.room;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserRegistry;
import com.ikar.ikarserver.backend.domain.kurento.room.RoomUserSession;
import com.ikar.ikarserver.backend.exception.websocket.RoomException;
import lombok.RequiredArgsConstructor;
import org.kurento.client.IceCandidate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

import static com.ikar.ikarserver.backend.util.Messages.CALL_USER_NOT_EXIST;

@Component
@RequiredArgsConstructor
public class PresentationOnIceCandidateRoomMessageHandler implements RoomMessageHandler {

    private final RoomUserRegistry registry;

    @Override
    public void process(JsonObject message, WebSocketSession session) throws IOException {
        final RoomUserSession user = registry.getBySession(session)
                .orElseThrow(RoomException.supplier(CALL_USER_NOT_EXIST));
        JsonObject candidateMessage = message.get("candidate").getAsJsonObject();

        if (user != null) {
            IceCandidate candidate = new IceCandidate(
                    candidateMessage.get("candidate").getAsString(),
                    candidateMessage.get("sdpMid").getAsString(),
                    candidateMessage.get("sdpMLineIndex").getAsInt()
            );
            user.addPresentationCandidate(candidate);
        }
    }

    @Override
    public String getProcessedMessage() {
        return "presentationOnIceCandidate";
    }
}
