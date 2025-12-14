package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private LocalDate dateOfBirth;
    private String gender;
    private String goalType;
    private BigDecimal goalWeightKg;
    private Integer dailyCalorieGoal;
}

