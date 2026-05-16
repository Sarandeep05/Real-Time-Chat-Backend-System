package com.example.chat_backend.service;

import com.example.chat_backend.dto.MessageResponse;
import com.example.chat_backend.model.Message;
import com.example.chat_backend.model.MessageStatus;
import com.example.chat_backend.model.MessageType;
import com.example.chat_backend.model.User;
import com.example.chat_backend.repository.MessageRepository;
import com.example.chat_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Persist a direct (1-to-1) message to MongoDB.
     */
    public Message saveDirectMessage(String senderId, String receiverId, String content) {
        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .type(MessageType.DIRECT)
                .content(content)
                .timestamp(Instant.now())
                .status(MessageStatus.SENT)
                .build();
        return messageRepository.save(message);
    }

    /**
     * Persist a group message to MongoDB.
     */
    public Message saveGroupMessage(String senderId, String groupId, String content) {
        Message message = Message.builder()
                .senderId(senderId)
                .receiverId(groupId)
                .type(MessageType.GROUP)
                .content(content)
                .timestamp(Instant.now())
                .status(MessageStatus.SENT)
                .build();
        return messageRepository.save(message);
    }

    /**
     * Retrieve paginated 1-to-1 chat history between two users.
     */
    public Page<MessageResponse> getChatHistory(String user1, String user2, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Message> messages = messageRepository.findChatHistory(user1, user2, pageable);
        return enrichMessages(messages);
    }

    /**
     * Retrieve paginated group message history.
     */
    public Page<MessageResponse> getGroupMessages(String groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Message> messages = messageRepository.findByReceiverIdAndType(groupId, MessageType.GROUP, pageable);
        return enrichMessages(messages);
    }

    /**
     * Mark a specific message as READ and return the updated message.
     */
    public Message markAsRead(String messageId, String readerId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        // Only the intended receiver can mark as read
        if (readerId.equals(message.getReceiverId())) {
            message.setStatus(MessageStatus.READ);
            return messageRepository.save(message);
        }
        return message;
    }

    /**
     * Mark all messages sent to a receiver as DELIVERED.
     */
    public void markMessagesDelivered(String receiverId) {
        messageRepository.findBySentToAndStatus(receiverId, MessageStatus.SENT)
                .forEach(msg -> {
                    msg.setStatus(MessageStatus.DELIVERED);
                    messageRepository.save(msg);
                });
    }

    /**
     * Enrich message page with sender names by doing a single batch user lookup.
     */
    private Page<MessageResponse> enrichMessages(Page<Message> messages) {
        // Collect unique sender IDs for a batch lookup
        var senderIds = messages.stream()
                .map(Message::getSenderId)
                .collect(Collectors.toSet());

        Map<String, String> senderNames = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, User::getName));

        return messages.map(msg -> MessageResponse.builder()
                .id(msg.getId())
                .senderId(msg.getSenderId())
                .senderName(senderNames.getOrDefault(msg.getSenderId(), "Unknown"))
                .receiverId(msg.getReceiverId())
                .type(msg.getType())
                .content(msg.getContent())
                .timestamp(msg.getTimestamp())
                .status(msg.getStatus())
                .build());
    }
}
