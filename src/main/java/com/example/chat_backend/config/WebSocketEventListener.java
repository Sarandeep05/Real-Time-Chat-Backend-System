package com.example.chat_backend.config;

import com.example.chat_backend.model.UserStatus;
import com.example.chat_backend.repository.UserRepository;
import com.example.chat_backend.security.CustomUserDetails;
import com.example.chat_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Listens to WebSocket session lifecycle events to maintain user online/offline status
 * and handle missed message delivery on reconnection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

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

            // Mark all pending SENT messages as DELIVERED (missed messages on reconnect)
            messageService.markMessagesDelivered(userId);
            log.info("User {} connected and missed messages marked as DELIVERED", userId);

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

            log.info("User {} disconnected", userId);
        }
    }
}
