package com.example.chat_backend.repository;

import com.example.chat_backend.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupRepository extends MongoRepository<Group, String> {
    List<Group> findByMembersContaining(String userId);
}
