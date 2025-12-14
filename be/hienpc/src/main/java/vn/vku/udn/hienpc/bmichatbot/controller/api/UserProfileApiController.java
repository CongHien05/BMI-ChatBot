package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.request.ProfileUpdateRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ProfileResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserProfile;
import vn.vku.udn.hienpc.bmichatbot.repository.UserProfileRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class UserProfileApiController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserProfileApiController(UserRepository userRepository,
                                    UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @GetMapping
    @Operation(summary = "Get user profile", description = "Get profile information including goals for authenticated user")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userDetails.getUsername()));

        Optional<UserProfile> profileOpt = userProfileRepository.findByUserUserId(user.getUserId());
        
        if (profileOpt.isEmpty()) {
            // Return empty profile if not exists
            return ResponseEntity.ok(new ProfileResponse(null, null, null, null, null));
        }
        
        UserProfile profile = profileOpt.get();
        ProfileResponse response = new ProfileResponse(
                profile.getDateOfBirth(),
                profile.getGender(),
                profile.getGoalType(),
                profile.getGoalWeightKg(),
                profile.getDailyCalorieGoal()
        );
        
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Update user profile", description = "Update profile information and goals for authenticated user")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                              @Valid @RequestBody ProfileUpdateRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userDetails.getUsername()));

        UserProfile profile = userProfileRepository.findByUserUserId(user.getUserId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUser(user);
                    return p;
                });

        // Partial update: only update fields that are provided (not null)
        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        if (request.getGoalType() != null) {
            profile.setGoalType(request.getGoalType());
        }
        if (request.getGoalWeightKg() != null) {
            profile.setGoalWeightKg(request.getGoalWeightKg());
        }
        if (request.getDailyCalorieGoal() != null) {
            profile.setDailyCalorieGoal(request.getDailyCalorieGoal());
        }

        userProfileRepository.save(profile);

        return ResponseEntity.ok().build();
    }
}
