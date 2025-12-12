package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Weight Prediction Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeightPredictionResponse {
    
    /**
     * Historical weight data points
     */
    private List<WeightDataPoint> historicalData;
    
    /**
     * Predicted weight data points
     */
    private List<WeightDataPoint> predictions;
    
    /**
     * Model accuracy metrics
     */
    private ModelMetrics metrics;
    
    /**
     * Insights and recommendations
     */
    private String insights;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeightDataPoint {
        private LocalDate date;
        private Double weightKg;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelMetrics {
        private Double rSquared;        // R² (coefficient of determination)
        private Double slope;            // kg per day
        private Double intercept;        // initial weight
        private Integer dataPoints;      // number of measurements used
        private String trend;            // "DECREASING", "INCREASING", "STABLE"
    }
}

