package vn.vku.udn.hienpc.bmichatbot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    // All fields are optional for partial update
    private LocalDate dateOfBirth;

    private String gender;

    private String goalType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Goal weight must be greater than 0")
    private BigDecimal goalWeightKg;

    @Min(value = 0, message = "Daily calorie goal cannot be negative")
    private Integer dailyCalorieGoal;
}
