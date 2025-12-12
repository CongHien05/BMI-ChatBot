package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Achievement response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AchievementResponse {
    private Integer achievementId;
    private String achievementType;
    private String title;           // Display title
    private String description;     // Description
    private String iconName;        // Icon resource name
    private LocalDateTime achievedAt;
    private Boolean isUnlocked;
}

