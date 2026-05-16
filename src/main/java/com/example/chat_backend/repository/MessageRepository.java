package com.example.chat_backend.repository;

import com.example.chat_backend.model.Message;
import com.example.chat_backend.model.MessageStatus;
import com.example.chat_backend.model.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    // Find messages between two users (1-to-1 chat), ordered by timestamp
    @Query("{ $or: [ { 'senderId': ?0, 'receiverId': ?1, 'type': 'DIRECT' }, { 'senderId': ?1, 'receiverId': ?0, 'type': 'DIRECT' } ] }")
    Page<Message> findChatHistory(String user1, String user2, Pageable pageable);

    // Find group messages by groupId and type
    Page<Message> findByReceiverIdAndType(String groupId, MessageType type, Pageable pageable);

    // Find all SENT messages targeting a specific receiver (for delivery receipts on reconnect)
    @Query("{ 'receiverId': ?0, 'status': ?1, 'type': 'DIRECT' }")
    List<Message> findBySentToAndStatus(String receiverId, MessageStatus status);
}
