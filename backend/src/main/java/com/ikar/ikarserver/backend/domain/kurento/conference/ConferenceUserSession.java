package com.ikar.ikarserver.backend.domain.kurento.conference;

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
public class ConferenceUserSession implements Closeable {

    private final String uuid;
    private final String name;
    private final String conferenceUuid;

    private final WebSocketSession session;
    private WebRtcEndpoint webRtcEndpoint;

    public void sendMessage(JsonObject message) throws IOException {
        log.debug("Sending message from user with session Id '{}': {}", session.getId(), message);
        session.sendMessage(new TextMessage(message.toString()));
    }

    public void addCandidate(IceCandidate candidate) {
        webRtcEndpoint.addIceCandidate(candidate);
    }

    @Override
    public void close() throws IOException {
        webRtcEndpoint.release();
    }
}