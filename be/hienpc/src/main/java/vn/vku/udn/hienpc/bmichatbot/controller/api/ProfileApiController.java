package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.response.AchievementResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.StreakResponse;
import vn.vku.udn.hienpc.bmichatbot.service.AchievementService;
import vn.vku.udn.hienpc.bmichatbot.service.StreakService;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile API", description = "User profile, achievements, streak management")
@SecurityRequirement(name = "bearerAuth")
public class ProfileApiController {

    private final AchievementService achievementService;
    private final StreakService streakService;

    public ProfileApiController(AchievementService achievementService,
                                StreakService streakService) {
        this.achievementService = achievementService;
        this.streakService = streakService;
    }

    /**
     * Get user achievements (badges)
     */
    @GetMapping("/achievements")
    @Operation(summary = "Get user achievements", description = "Lấy danh sách thành tựu (unlocked + locked)")
    public ResponseEntity<List<AchievementResponse>> getAchievements(Authentication authentication) {
        String userEmail = authentication.getName();
        List<AchievementResponse> achievements = achievementService.getUserAchievements(userEmail);
        return ResponseEntity.ok(achievements);
    }

    /**
     * Get user streak info
     */
    @GetMapping("/streak")
    @Operation(summary = "Get user streak", description = "Lấy thông tin streak (current, longest, message)")
    public ResponseEntity<StreakResponse> getStreak(Authentication authentication) {
        String userEmail = authentication.getName();
        StreakResponse streak = streakService.getStreakInfo(userEmail);
        return ResponseEntity.ok(streak);
    }
}

