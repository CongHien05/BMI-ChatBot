package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.response.WeightPredictionResponse;
import vn.vku.udn.hienpc.bmichatbot.service.WeightPredictionService;

@RestController
@RequestMapping("/api/predictions")
@Tag(name = "Prediction API", description = "AI-powered predictions for weight, trends, and recommendations")
@SecurityRequirement(name = "bearerAuth")
public class PredictionApiController {

    private final WeightPredictionService weightPredictionService;

    public PredictionApiController(WeightPredictionService weightPredictionService) {
        this.weightPredictionService = weightPredictionService;
    }

    /**
     * Predict weight for next N days using Linear Regression
     */
    @GetMapping("/weight")
    @Operation(
            summary = "Predict future weight",
            description = "Uses Linear Regression to predict weight for the next N days based on historical measurements. Requires at least 3 measurements."
    )
    public ResponseEntity<WeightPredictionResponse> predictWeight(
            Authentication authentication,
            @Parameter(description = "Number of days to predict (default: 7, max: 30)")
            @RequestParam(defaultValue = "7") int days
    ) {
        String userEmail = authentication.getName();
        
        // Validate days parameter
        if (days < 1 || days > 30) {
            throw new IllegalArgumentException("Days must be between 1 and 30");
        }
        
        WeightPredictionResponse prediction = weightPredictionService.predictWeight(userEmail, days);
        return ResponseEntity.ok(prediction);
    }
}

