package com.ikar.ikarserver.backend.domain.kurento.conference;

import com.google.gson.JsonObject;
import com.ikar.ikarserver.backend.util.ConferenceSender;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.IceCandidateFoundEvent;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
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
    private final String username;
    private final String conferenceIdentifier;
    private final WebSocketSession session;
    private WebRtcEndpoint webRtcEndpoint;

    public void sendMessage(JsonObject message) throws IOException {
        synchronized (session) {
            session.sendMessage(new TextMessage(message.toString()));
        }
    }

    public void addCandidate(IceCandidate candidate) {
        if (webRtcEndpoint != null) {
            webRtcEndpoint.addIceCandidate(candidate);
        }
    }

    public EventListener<IceCandidateFoundEvent> getCandidateEventListener() {
        return event -> {
            try {
                ConferenceSender.sendIceCandidate(this, event);
            } catch (IOException e) {
                log.debug(e.getMessage());
            }
        };
    }

    @Override
    public void close() {
        if (webRtcEndpoint != null) {
            webRtcEndpoint.release();
            webRtcEndpoint = null;
        }
    }

}
