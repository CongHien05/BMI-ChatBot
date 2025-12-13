package vn.vku.udn.hienpc.bmichatbot.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.response.IntentClassificationResponse;
import vn.vku.udn.hienpc.bmichatbot.ml.IntentClassifierService;
import vn.vku.udn.hienpc.bmichatbot.ml.IntentClassifierService.IntentResult;
import vn.vku.udn.hienpc.bmichatbot.ml.IntentType;

/**
 * API Controller for testing Intent Classification
 */
@RestController
@RequestMapping("/api/intent")
public class IntentApiController {
    
    private final IntentClassifierService intentClassifier;
    
    public IntentApiController(IntentClassifierService intentClassifier) {
        this.intentClassifier = intentClassifier;
    }
    
    /**
     * Test intent classification
     * GET /api/intent/classify?message=tôi muốn log bữa sáng
     */
    @GetMapping("/classify")
    public ResponseEntity<IntentClassificationResponse> classifyIntent(
            @RequestParam String message
    ) {
        IntentResult result = intentClassifier.classifyWithConfidence(message);
        IntentType intent = result.getIntent();
        double confidence = result.getConfidence();
        
        IntentClassificationResponse response = IntentClassificationResponse.builder()
                .message(message)
                .intent(intent.name())
                .intentDescription(intent.getDescription())
                .confidence(confidence)
                .suggestedResponse(getSuggestedResponse(intent))
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    private String getSuggestedResponse(IntentType intent) {
        switch (intent) {
            case LOG_FOOD:
                return "Navigate to Food Log screen";
            case GET_FOOD_RECOMMENDATION:
                return "Show AI food recommendations";
            case LOG_EXERCISE:
                return "Navigate to Exercise Log screen";
            case GET_EXERCISE_RECOMMENDATION:
                return "Show AI exercise recommendations";
            case VIEW_WEIGHT:
            case VIEW_BMI:
            case VIEW_DASHBOARD:
                return "Navigate to Dashboard";
            case PREDICT_WEIGHT:
                return "Show weight prediction chart";
            case VIEW_ACHIEVEMENTS:
                return "Navigate to Profile";
            case GREETING:
                return "Show welcome message";
            case HELP:
                return "Show help menu";
            default:
                return "Show general help";
        }
    }
}

