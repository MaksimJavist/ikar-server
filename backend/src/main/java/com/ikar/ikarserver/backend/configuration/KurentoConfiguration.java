package com.ikar.ikarserver.backend.configuration;

import com.ikar.ikarserver.backend.domain.kurento.RoomManager;
import com.ikar.ikarserver.backend.handler.RoomHandler;
import com.ikar.ikarserver.backend.service.RoomChatMessageService;
import lombok.RequiredArgsConstructor;
import org.kurento.client.KurentoClient;
import org.kurento.client.KurentoClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class KurentoConfiguration implements WebSocketConfigurer {

    private final RoomHandler roomHandler;

    public KurentoConfiguration(@Lazy RoomHandler roomHandler) {
        this.roomHandler = roomHandler;
    }

    @Bean
    public KurentoClient kurentoClient() {
        return new KurentoClientBuilder().setKmsWsUri("ws://localhost:8888/kurento").connect();
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(32768);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(roomHandler, "/groupcall").setAllowedOriginPatterns("*");
    }

}
