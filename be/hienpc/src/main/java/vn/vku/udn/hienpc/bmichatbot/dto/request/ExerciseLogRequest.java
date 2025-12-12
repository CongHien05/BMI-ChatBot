package vn.vku.udn.hienpc.bmichatbot.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseLogRequest {

    @NotNull(message = "exerciseId is required")
    private Integer exerciseId;

    @NotNull(message = "durationMinutes is required")
    @Min(value = 1, message = "Duration must be greater than 0")
    private Integer durationMinutes;
}
