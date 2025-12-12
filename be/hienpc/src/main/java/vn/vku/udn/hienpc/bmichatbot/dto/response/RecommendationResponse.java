package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Recommendation Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    
    private List<RecommendedItem> items;
    private String explanation;
    private Integer totalSimilarUsers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedItem {
        private Integer id;
        private String name;
        private String reason;          // Why recommended
        private Double score;            // Recommendation score (0-1)
        private Integer popularityCount; // How many similar users chose this
        private String unit;             // For food: serving unit, for exercise: activity type
        private Integer calories;        // Calories per unit
    }
}

