package com.ikar.ikarserver.backend.domain.kurento.newconference;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;

@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class UserSession implements Closeable {

    private final String uuid;
    private final String username;
    private final String conferenceIdentifier;
    private final WebSocketSession session;
    private WebRtcEndpoint webRtcEndpoint;

    public WebSocketSession getSession() {
        return session;
    }

    public void sendMessage(JsonObject message) throws IOException {
        log.debug("Sending message from user with session Id '{}': {}", session.getId(), message);
        session.sendMessage(new TextMessage(message.toString()));
    }

    public void addCandidate(IceCandidate candidate) {
        webRtcEndpoint.addIceCandidate(candidate);
    }

    @Override
    public void close() {
        if (webRtcEndpoint != null) {
            webRtcEndpoint.release();
            webRtcEndpoint = null;
        }
    }

}
