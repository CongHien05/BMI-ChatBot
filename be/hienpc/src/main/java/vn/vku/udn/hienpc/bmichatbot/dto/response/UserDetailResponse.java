package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
   
	public UserDetailResponse(Integer userId2, String email2, String fullName2, String name, Object object,
			Object object2, boolean b, Object object3, Object object4, Integer integer, long totalFoodLogs2,
			long totalExerciseLogs2, long totalAchievements2, long customFoodsCount2, long customExercisesCount2) {
		// TODO Auto-generated constructor stub
	}
	private Integer userId;
    private String email;
    private String fullName;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean active;

    // Profile data
    private String gender;
    private Double goalWeightKg;
    private Integer dailyCalorieGoal;

    // Statistics
    private Long totalFoodLogs;
    private Long totalExerciseLogs;
    private Long totalAchievements;
    private Long customFoodsCount;
    private Long customExercisesCount;
}
