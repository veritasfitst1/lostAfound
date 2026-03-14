package com.example.server.repository;

import com.example.server.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE (m.sender.id = :userId OR m.receiver.id = :userId) ORDER BY m.createdAt DESC")
    List<Message> findByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE ((m.sender.id = :u1 AND m.receiver.id = :u2) OR (m.sender.id = :u2 AND m.receiver.id = :u1)) ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("u1") Long userId1, @Param("u2") Long userId2);

    long countByReceiverIdAndIsRead(Long receiverId, Integer isRead);
}
