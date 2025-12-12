package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Food log history item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodLogHistoryResponse {
    private Integer logId;
    private Integer foodId;
    private String foodName;
    private Double quantity;
    private String unit;
    private Integer caloriesPerUnit;
    private Integer totalCalories;
    private LocalDateTime dateEaten;
}

