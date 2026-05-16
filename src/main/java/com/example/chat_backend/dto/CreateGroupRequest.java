package com.example.chat_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    private String name;

    @NotEmpty(message = "Members list cannot be empty")
    private List<String> memberIds;
}
