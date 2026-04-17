package com.frameasy.controller;

import com.frameasy.dto.ApiResponse;
import com.frameasy.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * AI Farming Assistant - responds only to farming-related queries.
 * Domain-restricted; extend with actual AI API (e.g. OpenAI) as needed.
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AiAssistantController {

    private static final String FARMING_ONLY_MESSAGE = "I can only help with farming-related questions: soil types, crop suggestions, weather guidance, and best practices. Please ask something related to agriculture.";

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<Map<String, String>>> chat(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String question = body != null ? body.get("message") : "";
        if (question == null) question = "";
        String reply = getFarmingReply(question.trim().toLowerCase());
        return ResponseEntity.ok(ApiResponse.success(Map.of("reply", reply)));
    }

    private String getFarmingReply(String q) {
        if (q.isEmpty()) return "Ask me anything about farming: soil, crops, weather, or best practices.";
        // Domain check: reject clearly non-farming queries
        Set<String> nonFarming = Set.of("movie", "cricket", "recipe", "song", "game", "stock", "political", "sport");
        if (nonFarming.stream().anyMatch(q::contains)) {
            return FARMING_ONLY_MESSAGE;
        }
        // Simple keyword-based responses (production: use LLM with farming context)
        if (q.contains("soil") || q.contains("land")) {
            return "Soil type affects crop choice. Clay soil holds water—good for rice; sandy soil drains well—suitable for pulses. Get your soil tested at the nearest agriculture office for precise recommendations.";
        }
        if (q.contains("crop") || q.contains("suggest") || q.contains("plant")) {
            return "Crop choice depends on season, soil, and water. Kharif: paddy, maize, cotton. Rabi: wheat, mustard, gram. Summer: vegetables, moong. Check your state agriculture department for local varieties.";
        }
        if (q.contains("weather") || q.contains("rain") || q.contains("monsoon")) {
            return "Check IMD (India Meteorological Department) or state agri weather bulletins before sowing and harvesting. Avoid spraying during high wind or rain.";
        }
        if (q.contains("fertilizer") || q.contains("fertiliser") || q.contains("nutrient")) {
            return "Use soil test results to apply NPK and micronutrients. Organic options: compost, vermicompost, green manure. Follow recommended doses to avoid soil damage.";
        }
        if (q.contains("pesticide") || q.contains("weed") || q.contains("pest")) {
            return "Use integrated pest management: resistant varieties, crop rotation, and pesticides only when needed. Follow label instructions and safe waiting period before harvest.";
        }
        if (q.contains("water") || q.contains("irrigation")) {
            return "Drip and sprinkler irrigation save water. Schedule irrigation based on crop stage and soil moisture. Rain-fed areas: choose drought-tolerant varieties.";
        }
        if (q.contains("loan") || q.contains("scheme") || q.contains("subsidy")) {
            return "Visit the Schemes section on FARM EASY or your state agriculture department website for current schemes, subsidies, and loan information.";
        }
        // Default: encourage farming topic
        return "I'm your farming assistant. You can ask about soil types, crop suggestions, weather, irrigation, fertilizers, or best practices. What would you like to know?";
    }
}
