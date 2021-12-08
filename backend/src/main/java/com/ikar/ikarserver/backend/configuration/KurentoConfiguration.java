package com.ikar.ikarserver.backend.configuration;

import com.ikar.ikarserver.backend.domain.kurento.CallHandler;
import com.ikar.ikarserver.backend.domain.kurento.RoomManager;
import com.ikar.ikarserver.backend.domain.kurento.UserRegistry;
import com.ikar.ikarserver.backend.service.AuthInfoService;
import com.ikar.ikarserver.backend.service.RoomIdentifierService;
import com.ikar.ikarserver.backend.service.impl.AuthInfoServiceImpl;
import com.ikar.ikarserver.backend.service.impl.RoomIdentifierServiceImpl;
import org.kurento.client.KurentoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class KurentoConfiguration {

    @Bean
    public RoomManager roomManager() {
        return new RoomManager(
                kurentoClient(),
                roomIdentifierService(),
                authInfoServiceImpl()
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
    public CallHandler callHandler() {
        return new CallHandler(
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

}
