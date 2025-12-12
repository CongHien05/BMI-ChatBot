package vn.vku.udn.hienpc.bmichatbot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementRequest {

    @NotNull(message = "weightKg is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "weightKg must be greater than 0")
    private BigDecimal weightKg;

    @DecimalMin(value = "0.0", inclusive = false, message = "heightCm must be greater than 0")
    private BigDecimal heightCm;

    private LocalDate dateRecorded;
}

