package com.example.chat_backend.controller;

import com.example.chat_backend.dto.MessageResponse;
import com.example.chat_backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    /**
     * GET /chats/direct?user1={id}&user2={id}&page=0&size=20
     * Retrieve paginated 1-to-1 chat history between two users.
     */
    @GetMapping("/direct")
    public ResponseEntity<Page<MessageResponse>> getDirectHistory(
            @RequestParam String user1,
            @RequestParam String user2,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(messageService.getChatHistory(user1, user2, page, size));
    }

    /**
     * GET /chats/group/{groupId}?page=0&size=20
     * Retrieve paginated group message history.
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Page<MessageResponse>> getGroupHistory(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(messageService.getGroupMessages(groupId, page, size));
    }
}
