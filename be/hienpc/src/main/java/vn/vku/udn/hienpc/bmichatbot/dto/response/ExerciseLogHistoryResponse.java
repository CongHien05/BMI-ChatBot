package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Exercise log history item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseLogHistoryResponse {
    private Integer logId;
    private Integer exerciseId;
    private String exerciseName;
    private Double durationMinutes;
    private Integer caloriesPerMinute;
    private Integer totalCaloriesBurned;
    private LocalDateTime dateExercised;
}

