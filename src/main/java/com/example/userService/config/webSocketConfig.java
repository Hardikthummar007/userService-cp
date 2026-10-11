package com.example.userService.config;

import com.example.userService.battle.websocket.StompAuthChannelInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class webSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private StompAuthChannelInterceptor stompAuthChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable in-memory broker for public topics and private queues
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix for messages routed to @MessageMapping controllers
        config.setApplicationDestinationPrefixes("/app");
        // Prefix for user-specific queues (/user/{username}/queue/...)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint with SockJS support
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:8084", "http://127.0.0.1:8084")
                .withSockJS();

        // Direct WebSocket endpoint without SockJS (for standard tools/clients)
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:5173", "http://localhost:8084", "http://127.0.0.1:8084");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register authentication interceptor to inspect STOMP CONNECT frame
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
