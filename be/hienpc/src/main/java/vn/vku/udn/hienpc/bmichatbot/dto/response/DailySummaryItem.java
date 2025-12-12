package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Daily summary item cho weekly/monthly charts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryItem {
    private LocalDate date;
    private Double weight;           // Weight của ngày đó (nếu có measurement)
    private Integer totalCalories;   // Tổng calories intake
    private Integer caloriesBurned;  // Tổng calories burned từ exercise
    private Integer netCalories;     // totalCalories - caloriesBurned
    private Integer foodLogsCount;   // Số lần log food
    private Integer exerciseLogsCount; // Số lần log exercise
}

