package com.frameasy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private User sender;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", insertable = false, updatable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "related_type", length = 50)
    private String relatedType; // EQUIPMENT, LAND, TRADE

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
