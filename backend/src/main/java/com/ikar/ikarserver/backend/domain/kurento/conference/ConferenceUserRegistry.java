package com.ikar.ikarserver.backend.domain.kurento.conference;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ConferenceUserRegistry {

    private final ConcurrentMap<String, Conference> conferenceBySessionId = new ConcurrentHashMap<>();

    public void register(ConferenceUserSession user, Conference conference) {
        String sessionId = user.getSession().getId();
        conferenceBySessionId.put(sessionId, conference);
    }

    public Optional<Conference> getConferenceBySession(WebSocketSession session) {
        return Optional.ofNullable(
                conferenceBySessionId.get(session.getId())
        );
    }

    public void removeBySession(WebSocketSession session) {
        final String sessionId = session.getId();
        conferenceBySessionId.remove(sessionId);
    }

}
