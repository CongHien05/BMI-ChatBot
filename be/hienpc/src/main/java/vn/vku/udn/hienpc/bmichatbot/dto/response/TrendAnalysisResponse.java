package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Trend analysis - phân tích xu hướng weight/calories
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysisResponse {
    private String weightTrend;         // "LOSING" | "GAINING" | "STABLE"
    private Double weightChangeRate;    // kg/week (trung bình)
    private String caloriesTrend;       // "INCREASING" | "DECREASING" | "STABLE"
    private Double avgDailyCalories;    
    private String activityTrend;       // "INCREASING" | "DECREASING" | "STABLE"
    private Double avgWeeklyExercises;  // Số lần tập/tuần
    
    // Insights/suggestions
    private String insight;             // Text suggestion cho user
    private Boolean onTrack;            // Có đúng hướng với goal không
    private Integer daysToGoal;         // Số ngày dự kiến đạt goal (nếu có target)
}

