package com.ikar.ikarserver.backend.domain.kurento.room;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomUserRegistry {

    private final ConcurrentHashMap<String, Room> roomBySessionId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RoomUserSession> usersBySessionId = new ConcurrentHashMap<>();

    public void register(RoomUserSession user, Room room) {
        String sessionId = user.getSession().getId();
        usersBySessionId.put(sessionId, user);
        roomBySessionId.put(sessionId, room);
    }

    public RoomUserSession getBySessionAndUuid(String uuid, WebSocketSession session) {
        final Room room = roomBySessionId.get(session.getId());
        return room.getParticipant(uuid);
    }

    public RoomUserSession getBySession(WebSocketSession session) {
        return usersBySessionId.get(session.getId());
    }

    public boolean existsByUuidInUserRoom(String uuid, WebSocketSession session) {
        final Room room = roomBySessionId.get(session.getId());
        return room.existParticipant(uuid);
    }

    public RoomUserSession removeBySession(WebSocketSession session) {
        final RoomUserSession user = getBySession(session);
        final String sessionId = session.getId();
        roomBySessionId.remove(sessionId);
        usersBySessionId.remove(sessionId);
        return user;
    }

}
