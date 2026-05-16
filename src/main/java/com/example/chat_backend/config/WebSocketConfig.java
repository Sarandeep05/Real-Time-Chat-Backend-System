package com.example.chat_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures STOMP-over-WebSocket.
 *
 * - Clients connect via: ws://host/ws  (with SockJS fallback at /ws)
 * - Application destination prefix: /app
 * - Topic broker prefix:  /topic  (group/broadcast destinations)
 * - Queue broker prefix:  /user   (user-specific / private destinations via convertAndSendToUser)
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable the simple in-memory broker for /topic and /queue prefixes
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefix for @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
        // Prefix used by convertAndSendToUser()
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Native WebSocket endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
        // SockJS fallback endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register the JWT auth interceptor for all inbound WebSocket messages
        registration.interceptors(webSocketAuthInterceptor);
    }
}
