package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.response.RecommendationResponse;
import vn.vku.udn.hienpc.bmichatbot.service.RecommendationService;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendation API", description = "AI-powered food and exercise recommendations using Collaborative Filtering")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationApiController {

    private final RecommendationService recommendationService;

    public RecommendationApiController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Get food recommendations
     */
    @GetMapping("/foods")
    @Operation(
            summary = "Get food recommendations",
            description = "Uses Collaborative Filtering to recommend foods based on similar users' preferences"
    )
    public ResponseEntity<RecommendationResponse> recommendFoods(
            Authentication authentication,
            @Parameter(description = "Number of recommendations (default: 10, max: 20)")
            @RequestParam(defaultValue = "10") int limit
    ) {
        String userEmail = authentication.getName();
        
        if (limit < 1 || limit > 20) {
            throw new IllegalArgumentException("Limit must be between 1 and 20");
        }
        
        RecommendationResponse response = recommendationService.recommendFoods(userEmail, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get exercise recommendations
     */
    @GetMapping("/exercises")
    @Operation(
            summary = "Get exercise recommendations",
            description = "Uses Collaborative Filtering to recommend exercises based on similar users' preferences"
    )
    public ResponseEntity<RecommendationResponse> recommendExercises(
            Authentication authentication,
            @Parameter(description = "Number of recommendations (default: 10, max: 20)")
            @RequestParam(defaultValue = "10") int limit
    ) {
        String userEmail = authentication.getName();
        
        if (limit < 1 || limit > 20) {
            throw new IllegalArgumentException("Limit must be between 1 and 20");
        }
        
        RecommendationResponse response = recommendationService.recommendExercises(userEmail, limit);
        return ResponseEntity.ok(response);
    }
}

