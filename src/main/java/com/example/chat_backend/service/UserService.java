package com.example.chat_backend.service;

import com.example.chat_backend.dto.UserResponse;
import com.example.chat_backend.model.User;
import com.example.chat_backend.model.UserStatus;
import com.example.chat_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void setUserOnline(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(UserStatus.ONLINE);
            userRepository.save(user);
        });
    }

    public void setUserOffline(String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(UserStatus.OFFLINE);
            user.setLastSeen(Instant.now());
            userRepository.save(user);
        });
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus() != null ? user.getStatus().name() : UserStatus.OFFLINE.name())
                .lastSeen(user.getLastSeen())
                .build();
    }
}
