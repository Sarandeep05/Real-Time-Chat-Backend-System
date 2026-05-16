package com.example.chat_backend.service;

import com.example.chat_backend.dto.CreateGroupRequest;
import com.example.chat_backend.model.Group;
import com.example.chat_backend.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    public Group createGroup(String creatorId, CreateGroupRequest request) {
        List<String> members = new ArrayList<>(request.getMemberIds());
        // Ensure creator is always a member
        if (!members.contains(creatorId)) {
            members.add(creatorId);
        }

        Group group = Group.builder()
                .name(request.getName())
                .members(members)
                .createdBy(creatorId)
                .createdAt(Instant.now())
                .build();

        return groupRepository.save(group);
    }

    public Group addMember(String groupId, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        if (!group.getMembers().contains(userId)) {
            group.getMembers().add(userId);
            return groupRepository.save(group);
        }
        return group;
    }

    public Group removeMember(String groupId, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));

        group.getMembers().remove(userId);
        return groupRepository.save(group);
    }

    public Group getGroupById(String groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }

    public List<Group> getGroupsByMember(String userId) {
        return groupRepository.findByMembersContaining(userId);
    }
}
