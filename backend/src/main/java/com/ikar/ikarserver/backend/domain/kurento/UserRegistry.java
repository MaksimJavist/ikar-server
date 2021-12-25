package com.ikar.ikarserver.backend.domain.kurento;

import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

public class UserRegistry {

    private final ConcurrentHashMap<String, Room> roomBySessionId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();

    public void register(UserSession user, Room room) {
        String sessionId = user.getSession().getId();
        usersBySessionId.put(sessionId, user);
        roomBySessionId.put(sessionId, room);
    }

    public UserSession getBySessionAndUuid(String uuid, WebSocketSession session) {
        final Room room = roomBySessionId.get(session.getId());
        return room.getParticipant(uuid);
    }

    public UserSession getBySession(WebSocketSession session) {
        return usersBySessionId.get(session.getId());
    }

    public boolean existsByUuidInUserRoom(String uuid, WebSocketSession session) {
        final Room room = roomBySessionId.get(session.getId());
        return room.existParticipant(uuid);
    }

    public UserSession removeBySession(WebSocketSession session) {
        final UserSession user = getBySession(session);
        final String sessionId = session.getId();
        roomBySessionId.remove(sessionId);
        usersBySessionId.remove(sessionId);
        return user;
    }

}
