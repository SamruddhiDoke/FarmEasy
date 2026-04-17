package com.frameasy.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ChatMessageDto {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String content;
    private String relatedType;
    private Long relatedId;
    private Instant createdAt;
    private Boolean isRead;
}
