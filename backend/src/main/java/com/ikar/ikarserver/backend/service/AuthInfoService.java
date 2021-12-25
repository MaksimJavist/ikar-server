package com.ikar.ikarserver.backend.service;

import com.ikar.ikarserver.backend.domain.AuthInfo;
import com.ikar.ikarserver.backend.domain.AuthUserInfo;
import lombok.NonNull;
import org.springframework.web.socket.WebSocketSession;

import java.util.Optional;

public interface AuthInfoService {

    AuthUserInfo getAuthUserInfo();

    AuthInfo getAuthInfo();

    Optional<String> getWebsocketUserUuid(@NonNull WebSocketSession session);

}
