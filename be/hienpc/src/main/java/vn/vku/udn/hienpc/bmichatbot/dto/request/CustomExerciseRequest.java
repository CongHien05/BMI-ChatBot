package vn.vku.udn.hienpc.bmichatbot.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomExerciseRequest {

    @NotBlank(message = "Exercise name is required")
    private String exerciseName;

    @NotNull(message = "Calories burned per hour is required")
    @Min(value = 1, message = "Calories must be greater than 0")
    private Integer caloriesBurnedPerHour;
}

