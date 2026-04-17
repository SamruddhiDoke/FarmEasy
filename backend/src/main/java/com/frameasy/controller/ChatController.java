package com.frameasy.controller;

import com.frameasy.dto.ApiResponse;
import com.frameasy.dto.ChatMessageDto;
import com.frameasy.security.UserPrincipal;
import com.frameasy.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversation")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long otherUserId) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(ApiResponse.success(chatService.getConversation(principal.getId(), otherUserId)));
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatMessageDto>> send(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody SendMessageRequest req) {
        if (principal == null) return ResponseEntity.status(401).build();
        ChatMessageDto dto = chatService.send(
                principal.getId(), req.getReceiverId(), req.getContent(),
                req.getRelatedType(), req.getRelatedId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/mark-read/{messageId}")
    public ResponseEntity<ApiResponse<Void>> markRead(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable Long messageId) {
        if (principal == null) return ResponseEntity.status(401).build();
        chatService.markRead(messageId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @lombok.Data
    public static class SendMessageRequest {
        private Long receiverId;
        private String content;
        private String relatedType;
        private Long relatedId;
    }
}
