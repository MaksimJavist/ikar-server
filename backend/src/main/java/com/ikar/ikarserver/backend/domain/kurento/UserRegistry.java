package com.ikar.ikarserver.backend.domain.kurento;

import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

public class UserRegistry {

    private final ConcurrentHashMap<String, UserSession> usersByUuid = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();

    public void register(UserSession user) {
        usersByUuid.put(user.getUuid(), user);
        usersBySessionId.put(user.getSession().getId(), user);
    }

    public UserSession getByUuid(String uuid) {
        return usersByUuid.get(uuid);
    }

    public UserSession getBySession(WebSocketSession session) {
        return usersBySessionId.get(session.getId());
    }

    public boolean exists(String uuid) {
        return usersByUuid.keySet().contains(uuid);
    }

    public UserSession removeBySession(WebSocketSession session) {
        final UserSession user = getBySession(session);
        usersByUuid.remove(user.getUuid());
        usersBySessionId.remove(session.getId());
        return user;
    }

}
