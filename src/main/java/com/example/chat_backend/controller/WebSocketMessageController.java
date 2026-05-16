package com.example.chat_backend.controller;

import com.example.chat_backend.dto.ChatMessage;
import com.example.chat_backend.dto.MessageResponse;
import com.example.chat_backend.dto.ReadReceiptMessage;
import com.example.chat_backend.model.Group;
import com.example.chat_backend.model.Message;
import com.example.chat_backend.model.User;
import com.example.chat_backend.repository.GroupRepository;
import com.example.chat_backend.repository.UserRepository;
import com.example.chat_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * Handles real-time messaging over WebSocket (STOMP).
 *
 * Client sends to:
 *   - /app/chat.send        → 1-to-1 message
 *   - /app/chat.group.send  → Group message
 *   - /app/chat.receipt     → Read receipt
 *
 * Server delivers to:
 *   - /user/{receiverId}/queue/messages  → recipient's private queue
 *   - /topic/group/{groupId}             → group topic
 *   - /user/{senderId}/queue/receipts    → read receipt confirmations
 *   - /topic/presence                    → online/offline events
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    /**
     * Handle direct (1-to-1) messages.
     * Validates receiver exists, persists the message, then routes it to the recipient's private queue.
     */
    @MessageMapping("/chat.send")
    public void handleDirectMessage(@Payload ChatMessage chatMessage) {
        // Edge case: Validate receiver exists
        Optional<User> receiver = userRepository.findById(chatMessage.getReceiverId());
        if (receiver.isEmpty()) {
            log.warn("Direct message to non-existent user: {}", chatMessage.getReceiverId());
            // Notify sender about the error
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getSenderId(),
                    "/queue/errors",
                    java.util.Map.of("error", "User not found: " + chatMessage.getReceiverId())
            );
            return;
        }

        // Persist message first (offline delivery guarantee)
        Message saved = messageService.saveDirectMessage(
                chatMessage.getSenderId(),
                chatMessage.getReceiverId(),
                chatMessage.getContent()
        );

        String senderName = userRepository.findById(saved.getSenderId())
                .map(User::getName)
                .orElse("Unknown");

        MessageResponse response = MessageResponse.builder()
                .id(saved.getId())
                .senderId(saved.getSenderId())
                .senderName(senderName)
                .receiverId(saved.getReceiverId())
                .type(saved.getType())
                .content(saved.getContent())
                .timestamp(saved.getTimestamp())
                .status(saved.getStatus())
                .build();

        // Deliver to recipient's private queue
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverId(),
                "/queue/messages",
                response
        );

        // Also echo back to sender so both sides see the message in real time
        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId(),
                "/queue/messages",
                response
        );
    }

    /**
     * Handle group messages.
     * Validates sender is a member of the group, persists the message,
     * then broadcasts it to the group topic.
     */
    @MessageMapping("/chat.group.send")
    public void handleGroupMessage(@Payload ChatMessage chatMessage) {
        String groupId = chatMessage.getReceiverId();

        // Edge case: Validate group exists
        Optional<Group> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            log.warn("Group message to non-existent group: {}", groupId);
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getSenderId(),
                    "/queue/errors",
                    java.util.Map.of("error", "Group not found: " + groupId)
            );
            return;
        }

        // Edge case: Validate sender is a member of the group
        Group group = groupOpt.get();
        if (!group.getMembers().contains(chatMessage.getSenderId())) {
            log.warn("Non-member {} tried to send message to group {}", chatMessage.getSenderId(), groupId);
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getSenderId(),
                    "/queue/errors",
                    java.util.Map.of("error", "You are not a member of this group")
            );
            return;
        }

        Message saved = messageService.saveGroupMessage(
                chatMessage.getSenderId(),
                groupId,
                chatMessage.getContent()
        );

        String senderName = userRepository.findById(saved.getSenderId())
                .map(User::getName)
                .orElse("Unknown");

        MessageResponse response = MessageResponse.builder()
                .id(saved.getId())
                .senderId(saved.getSenderId())
                .senderName(senderName)
                .receiverId(saved.getReceiverId())
                .type(saved.getType())
                .content(saved.getContent())
                .timestamp(saved.getTimestamp())
                .status(saved.getStatus())
                .build();

        // Broadcast to all subscribers of the group topic
        messagingTemplate.convertAndSend("/topic/group/" + groupId, response);
    }

    /**
     * Handle read receipts.
     * Marks the message as READ in DB and notifies the original sender.
     */
    @MessageMapping("/chat.receipt")
    public void handleReadReceipt(@Payload ReadReceiptMessage receipt) {
        Message updated = messageService.markAsRead(receipt.getMessageId(), receipt.getReaderId());

        // Notify the original sender that their message was read
        messagingTemplate.convertAndSendToUser(
                updated.getSenderId(),
                "/queue/receipts",
                updated
        );
    }
}
