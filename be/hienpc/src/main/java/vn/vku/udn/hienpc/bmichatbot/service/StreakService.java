package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import vn.vku.udn.hienpc.bmichatbot.dto.response.StreakResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserStreak;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserStreakRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class StreakService {

    private final UserStreakRepository streakRepository;
    private final UserRepository userRepository;
    private final AchievementService achievementService;

    public StreakService(UserStreakRepository streakRepository,
                         UserRepository userRepository,
                         AchievementService achievementService) {
        this.streakRepository = streakRepository;
        this.userRepository = userRepository;
        this.achievementService = achievementService;
    }

    /**
     * Get streak info for user
     */
    public StreakResponse getStreakInfo(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        UserStreak streak = streakRepository.findByUserUserId(user.getUserId())
                .orElse(createNewStreak(user));

        LocalDate today = LocalDate.now();
        boolean isActive = streak.getLastLogDate() != null && 
                          streak.getLastLogDate().equals(today);

        String message = generateStreakMessage(streak.getCurrentStreak(), isActive);

        return new StreakResponse(
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getLastLogDate(),
                message,
                isActive
        );
    }

    /**
     * Update streak when user logs food/exercise
     */
    public void updateStreak(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserStreak streak = streakRepository.findByUserUserId(userId)
                .orElse(createNewStreak(user));

        LocalDate today = LocalDate.now();
        LocalDate lastLog = streak.getLastLogDate();

        if (lastLog == null) {
            // First log ever
            streak.setCurrentStreak(1);
            streak.setLongestStreak(1);
            streak.setLastLogDate(today);
            streak.setUpdatedAt(today);
            
            achievementService.unlockAchievement(userId, "FIRST_LOG");
            
        } else if (lastLog.equals(today)) {
            // Already logged today - no change
            return;
            
        } else {
            long daysBetween = ChronoUnit.DAYS.between(lastLog, today);
            
            if (daysBetween == 1) {
                // Consecutive day - increment streak
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
                streak.setLastLogDate(today);
                streak.setUpdatedAt(today);
                
                // Check for streak achievements
                if (streak.getCurrentStreak() == 7) {
                    achievementService.unlockAchievement(userId, "7_DAY_STREAK");
                } else if (streak.getCurrentStreak() == 30) {
                    achievementService.unlockAchievement(userId, "30_DAY_STREAK");
                } else if (streak.getCurrentStreak() == 100) {
                    achievementService.unlockAchievement(userId, "100_DAY_STREAK");
                }
                
                // Update longest streak
                if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                    streak.setLongestStreak(streak.getCurrentStreak());
                }
                
            } else {
                // Streak broken - reset to 1
                streak.setCurrentStreak(1);
                streak.setLastLogDate(today);
                streak.setUpdatedAt(today);
            }
        }

        streakRepository.save(streak);
    }

    /**
     * Create new streak for user
     */
    private UserStreak createNewStreak(User user) {
        UserStreak streak = new UserStreak();
        streak.setUser(user);
        streak.setCurrentStreak(0);
        streak.setLongestStreak(0);
        streak.setLastLogDate(null);
        streak.setUpdatedAt(LocalDate.now());
        return streak;
    }

    /**
     * Generate motivational message based on streak
     */
    private String generateStreakMessage(Integer currentStreak, Boolean isActive) {
        if (!isActive) {
            return "Hãy log hôm nay để tiếp tục streak! 🔥";
        }
        
        if (currentStreak == 0) {
            return "Bắt đầu streak của bạn ngay hôm nay! 💪";
        } else if (currentStreak < 7) {
            return String.format("Tuyệt vời! Còn %d ngày nữa để đạt 7 ngày! 🔥", 7 - currentStreak);
        } else if (currentStreak < 30) {
            return String.format("Xuất sắc! Còn %d ngày nữa để đạt 30 ngày! ⭐", 30 - currentStreak);
        } else if (currentStreak < 100) {
            return String.format("Tuyệt vời! Còn %d ngày nữa để đạt 100 ngày! 👑", 100 - currentStreak);
        } else {
            return "Bạn là huyền thoại! Tiếp tục phát huy! 🏆";
        }
    }
}

