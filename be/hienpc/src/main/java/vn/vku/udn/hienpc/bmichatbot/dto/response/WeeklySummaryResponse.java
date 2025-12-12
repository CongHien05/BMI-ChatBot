package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Weekly summary response - chứa data 7 ngày gần nhất
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySummaryResponse {
    private List<DailySummaryItem> dailySummaries; // 7 items, sorted by date asc
    private Double averageCalories;     // Trung bình calories/ngày
    private Double averageWeight;       // Trung bình cân nặng (nếu có measurements)
    private Double weightChange;        // Thay đổi cân nặng (ngày cuối - ngày đầu)
    private Integer totalFoodLogs;      // Tổng số lần log food trong 7 ngày
    private Integer totalExerciseLogs;  // Tổng số lần log exercise
}

