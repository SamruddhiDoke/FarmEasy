package com.frameasy.service;

import com.frameasy.dto.ChatMessageDto;
import com.frameasy.model.ChatMessage;
import com.frameasy.model.User;
import com.frameasy.repository.ChatMessageRepository;
import com.frameasy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageDto send(Long senderId, Long receiverId, String content, String relatedType, Long relatedId) {
        ChatMessage msg = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .build();
        msg = chatMessageRepository.save(msg);
        return toDto(msg);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getConversation(Long user1, Long user2) {
        List<ChatMessage> list = chatMessageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(user1, user2, user2, user1);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public void markRead(Long messageId, Long readerId) {
        chatMessageRepository.findById(messageId).ifPresent(m -> {
            if (m.getReceiverId().equals(readerId)) {
                m.setReadAt(Instant.now());
                chatMessageRepository.save(m);
            }
        });
    }

    private ChatMessageDto toDto(ChatMessage m) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(m.getId());
        dto.setSenderId(m.getSenderId());
        dto.setReceiverId(m.getReceiverId());
        dto.setContent(m.getContent());
        dto.setRelatedType(m.getRelatedType());
        dto.setRelatedId(m.getRelatedId());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setIsRead(m.getReadAt() != null);
        userRepository.findById(m.getSenderId()).ifPresent(u -> dto.setSenderName(u.getName()));
        return dto;
    }
}
