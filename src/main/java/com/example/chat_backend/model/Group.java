package com.example.chat_backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "groups")
public class Group {
    @Id
    private String id;
    
    private String name;
    
    private List<String> members; // List of user IDs
    
    private String createdBy; // User ID
    
    private Instant createdAt;
}
