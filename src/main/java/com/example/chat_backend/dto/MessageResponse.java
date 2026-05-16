package com.example.chat_backend.dto;

import com.example.chat_backend.model.MessageStatus;
import com.example.chat_backend.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String id;
    private String senderId;
    private String senderName;
    private String receiverId;
    private MessageType type;
    private String content;
    private Instant timestamp;
    private MessageStatus status;
}
