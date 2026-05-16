package com.example.chat_backend.controller;

import com.example.chat_backend.dto.CreateGroupRequest;
import com.example.chat_backend.model.Group;
import com.example.chat_backend.security.CustomUserDetails;
import com.example.chat_backend.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * POST /groups
     * Create a new group. The authenticated user is automatically added as creator & member.
     */
    @PostMapping
    public ResponseEntity<Group> createGroup(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        Group group = groupService.createGroup(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    /**
     * GET /groups
     * List all groups the authenticated user is a member of.
     */
    @GetMapping
    public ResponseEntity<List<Group>> getMyGroups(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(groupService.getGroupsByMember(currentUser.getId()));
    }

    /**
     * GET /groups/{groupId}
     * Get group details by ID.
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroup(@PathVariable String groupId) {
        return ResponseEntity.ok(groupService.getGroupById(groupId));
    }

    /**
     * POST /groups/{groupId}/members/{userId}
     * Add a member to an existing group.
     */
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> addMember(
            @PathVariable String groupId,
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(groupService.addMember(groupId, userId));
    }

    /**
     * DELETE /groups/{groupId}/members/{userId}
     * Remove a member from an existing group.
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> removeMember(
            @PathVariable String groupId,
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(groupService.removeMember(groupId, userId));
    }
}
