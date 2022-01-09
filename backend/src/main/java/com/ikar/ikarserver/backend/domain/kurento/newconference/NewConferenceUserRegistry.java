package com.ikar.ikarserver.backend.domain.kurento.newconference;

import com.ikar.ikarserver.backend.domain.kurento.conference.Conference;
import com.ikar.ikarserver.backend.domain.kurento.conference.ConferenceUserSession;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class NewConferenceUserRegistry {

    private final ConcurrentMap<String, Conference> conferenceBySessionId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConferenceUserSession> usersBySessionId = new ConcurrentHashMap<>();

    public void register(ConferenceUserSession user, Conference conference) {
        String sessionId = user.getSession().getId();
        usersBySessionId.put(sessionId, user);
        conferenceBySessionId.put(sessionId, conference);
    }

    public ConferenceUserSession getBySessionAndUuid(String uuid, WebSocketSession session) {
        final Conference conference = conferenceBySessionId.get(session.getId());
        return conference.getUserBySession(session);
    }

    public Optional<ConferenceUserSession> getBySession(WebSocketSession session) {
        return Optional.ofNullable(
                usersBySessionId.get(session.getId())
        );
    }

    public void removeBySession(WebSocketSession session) {
        final String sessionId = session.getId();
        conferenceBySessionId.remove(sessionId);
        usersBySessionId.remove(sessionId);
    }

}
