package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private Long totalUsers;
    private Long totalFoods;
    private Long totalExercises;
    private Long totalFoodLogs;
    private Long totalExerciseLogs;
    private Long activeUsersToday;
    private Long activeUsersThisWeek;

    // Additional statistics
    private Long totalCustomFoods;
    private Long totalCustomExercises;
    private Long totalChatbotRules;
    private Long totalAuditLogs;
    private Long newUsersThisWeek;
    private Long foodLogsToday;
    private Long exerciseLogsToday;
}


