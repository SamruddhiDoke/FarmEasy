package com.frameasy.repository;

import com.frameasy.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
            Long s1, Long r1, Long s2, Long r2);
}
