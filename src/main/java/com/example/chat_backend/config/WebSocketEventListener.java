package com.example.chat_backend.config;

import com.example.chat_backend.model.UserStatus;
import com.example.chat_backend.repository.UserRepository;
import com.example.chat_backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;

/**
 * Listens to WebSocket session lifecycle events to maintain user online/offline status.
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken authToken &&
                authToken.getPrincipal() instanceof CustomUserDetails userDetails) {

            String userId = userDetails.getId();
            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(UserStatus.ONLINE);
                userRepository.save(user);
            });

            // Broadcast presence update to all subscribers
            messagingTemplate.convertAndSend("/topic/presence",
                    (Object) Map.of("userId", userId, "status", "ONLINE"));
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken authToken &&
                authToken.getPrincipal() instanceof CustomUserDetails userDetails) {

            String userId = userDetails.getId();
            userRepository.findById(userId).ifPresent(user -> {
                user.setStatus(UserStatus.OFFLINE);
                user.setLastSeen(Instant.now());
                userRepository.save(user);
            });

            messagingTemplate.convertAndSend("/topic/presence",
                    (Object) Map.of("userId", userId, "status", "OFFLINE"));
        }
    }
}
