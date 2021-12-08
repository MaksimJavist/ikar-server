package com.ikar.ikarserver.backend.configuration;

import com.ikar.ikarserver.backend.domain.kurento.CallHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final CallHandler callHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(callHandler, "/groupcall").setAllowedOriginPatterns("*");
    }
}
