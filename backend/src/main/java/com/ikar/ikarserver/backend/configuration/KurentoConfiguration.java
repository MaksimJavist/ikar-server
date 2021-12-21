package com.ikar.ikarserver.backend.configuration;

import com.ikar.ikarserver.backend.handler.RoomHandler;
import com.ikar.ikarserver.backend.domain.kurento.RoomManager;
import com.ikar.ikarserver.backend.domain.kurento.UserRegistry;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.RoomChatMessageService;
import com.ikar.ikarserver.backend.service.RoomIdentifierService;
import com.ikar.ikarserver.backend.service.impl.AuthInfoServiceImpl;
import com.ikar.ikarserver.backend.service.impl.RoomIdentifierServiceImpl;
import lombok.RequiredArgsConstructor;
import org.kurento.client.KurentoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class KurentoConfiguration implements WebSocketConfigurer {

    private final RoomChatMessageService roomChatMessageService;

    @Bean
    public RoomManager roomManager() {
        return new RoomManager(
                kurentoClient(),
                roomIdentifierService(),
                authInfoServiceImpl(),
                roomChatMessageService
        );
    }

    @Bean
    public RoomIdentifierService roomIdentifierService() {
        return new RoomIdentifierServiceImpl();
    }

    @Bean
    public AuthInfoService authInfoServiceImpl() {
        return new AuthInfoServiceImpl();
    }

    @Bean
    public UserRegistry userRegistry() {
        return new UserRegistry();
    }

    @Bean
    public RoomHandler callHandler() {
        return new RoomHandler(
                roomManager(),
                userRegistry()
        );
    }

    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create();
    }

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(32768);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(callHandler(), "/groupcall").setAllowedOriginPatterns("*");
    }

}
