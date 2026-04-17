package com.frameasy.websocket;

import com.frameasy.dto.ChatMessageDto;
import com.frameasy.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket handler for real-time chat.
 * Client sends to /app/chat/{receiverId} with payload { content, relatedType?, relatedId? }
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/chat/{receiverId}")
    public void sendMessage(@DestinationVariable Long receiverId, @Payload Map<String, Object> payload) {
        Long senderId = payload.get("senderId") != null ? Long.valueOf(payload.get("senderId").toString()) : null;
        String content = (String) payload.get("content");
        String relatedType = (String) payload.get("relatedType");
        Long relatedId = payload.get("relatedId") != null ? Long.valueOf(payload.get("relatedId").toString()) : null;
        if (senderId == null || content == null) return;
        ChatMessageDto dto = chatService.send(senderId, receiverId, content, relatedType, relatedId);
        messagingTemplate.convertAndSend("/queue/chat/" + receiverId, dto);
        messagingTemplate.convertAndSend("/queue/chat/" + senderId, dto);
    }
}
