package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Monthly summary response - chứa data 30 ngày gần nhất
 * Có thể group by week để giảm số data points cho chart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryResponse {
    private List<DailySummaryItem> dailySummaries; // 30 items, sorted by date asc
    private Double averageCalories;     // Trung bình calories/ngày
    private Double averageWeight;       // Trung bình cân nặng
    private Double weightChange;        // Thay đổi cân nặng trong 30 ngày
    private Integer totalFoodLogs;      
    private Integer totalExerciseLogs;
    private Double bmiChange;           // Thay đổi BMI (nếu có)
}

