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
public class CustomFoodRequest {

    @NotBlank(message = "Food name is required")
    private String foodName;

    @NotBlank(message = "Serving unit is required")
    private String servingUnit;

    @NotNull(message = "Calories per unit is required")
    @Min(value = 1, message = "Calories must be greater than 0")
    private Integer caloriesPerUnit;
}

