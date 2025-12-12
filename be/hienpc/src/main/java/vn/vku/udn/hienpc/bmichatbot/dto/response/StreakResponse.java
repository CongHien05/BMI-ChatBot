package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Streak response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreakResponse {
    private Integer currentStreak;      // Streak hiện tại
    private Integer longestStreak;      // Streak dài nhất
    private LocalDate lastLogDate;      // Ngày log gần nhất
    private String message;             // Motivational message
    private Boolean isActive;           // Có đang active streak không (logged today)
}

