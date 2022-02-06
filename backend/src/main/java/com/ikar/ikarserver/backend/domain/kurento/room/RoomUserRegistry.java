package com.ikar.ikarserver.backend.domain.kurento.room;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomUserRegistry {

    private final ConcurrentHashMap<String, RoomUserSession> usersBySessionId = new ConcurrentHashMap<>();

    public void register(RoomUserSession user) {
        String sessionId = user.getSession().getId();
        usersBySessionId.put(sessionId, user);
    }

    public Optional<RoomUserSession> getBySession(WebSocketSession session) {
        return Optional.ofNullable(
                usersBySessionId.get(session.getId())
        );
    }

    public RoomUserSession removeBySession(WebSocketSession session) {
        final RoomUserSession user = getBySession(session).get();
        final String sessionId = session.getId();
        usersBySessionId.remove(sessionId);
        return user;
    }

}
