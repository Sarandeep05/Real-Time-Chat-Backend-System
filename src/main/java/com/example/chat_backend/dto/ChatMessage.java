package com.example.chat_backend.dto;

import com.example.chat_backend.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String senderId;
    private String receiverId; // userId for DIRECT, groupId for GROUP
    private MessageType type;
    private String content;
}
