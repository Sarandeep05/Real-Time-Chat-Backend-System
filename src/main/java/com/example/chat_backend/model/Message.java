package com.example.chat_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    
    @Indexed
    private String senderId;
    
    @Indexed
    private String receiverId; // Can be user ID or group ID depending on type
    
    private MessageType type;
    
    private String content;
    
    @Indexed
    private Instant timestamp;
    
    private MessageStatus status;
}
