package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.request.CustomExerciseRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.request.ExerciseLogRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseLogHistoryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseResponse;
import vn.vku.udn.hienpc.bmichatbot.service.ExerciseLogService;
import vn.vku.udn.hienpc.bmichatbot.service.FavoriteService;
import vn.vku.udn.hienpc.bmichatbot.service.RecentService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExerciseLogApiController {

    private final ExerciseLogService exerciseLogService;
    private final FavoriteService favoriteService;
    private final RecentService recentService;

    public ExerciseLogApiController(ExerciseLogService exerciseLogService, FavoriteService favoriteService, RecentService recentService) {
        this.exerciseLogService = exerciseLogService;
        this.favoriteService = favoriteService;
        this.recentService = recentService;
    }

    @GetMapping("/exercises")
    @Operation(summary = "Get all exercises", description = "Return list of exercises for logging workouts (includes public exercises and user's custom exercises)")
    public ResponseEntity<List<ExerciseResponse>> getExercises(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Search query (optional)")
            @RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(exerciseLogService.searchExercises(userDetails.getUsername(), q));
        }
        return ResponseEntity.ok(exerciseLogService.getAllExercises(userDetails.getUsername()));
    }

    @PostMapping("/logs/exercise")
    @Operation(summary = "Log exercise", description = "Create an exercise log entry for the authenticated user")
    public ResponseEntity<Void> logExercise(@AuthenticationPrincipal UserDetails userDetails,
                                            @Valid @RequestBody ExerciseLogRequest request) {
        exerciseLogService.logExercise(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs/exercise/history")
    @Operation(summary = "Get exercise log history", description = "Get exercise logs filtered by date range")
    public ResponseEntity<List<ExerciseLogHistoryResponse>> getExerciseLogHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        List<ExerciseLogHistoryResponse> history = exerciseLogService.getExerciseLogHistory(
                userDetails.getUsername(), from, to);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/logs/exercise/{logId}")
    @Operation(summary = "Update exercise log", description = "Update an existing exercise log entry (only today's logs)")
    public ResponseEntity<Void> updateExerciseLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Log ID")
            @PathVariable Integer logId,
            @Valid @RequestBody ExerciseLogRequest request) {
        exerciseLogService.updateExerciseLog(userDetails.getUsername(), logId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logs/exercise/{logId}")
    @Operation(summary = "Delete exercise log", description = "Delete an exercise log entry (only today's logs)")
    public ResponseEntity<Void> deleteExerciseLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Log ID")
            @PathVariable Integer logId) {
        exerciseLogService.deleteExerciseLog(userDetails.getUsername(), logId);
        return ResponseEntity.ok().build();
    }

    // ========== FAVORITES ENDPOINTS ==========

    @GetMapping("/exercises/favorites")
    @Operation(summary = "Get favorite exercises", description = "Get list of user's favorite exercises")
    public ResponseEntity<List<ExerciseResponse>> getFavoriteExercises(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(favoriteService.getFavoriteExercises(userDetails.getUsername()));
    }

    @PostMapping("/exercises/favorites/{exerciseId}")
    @Operation(summary = "Add exercise to favorites", description = "Add an exercise to user's favorites")
    public ResponseEntity<Void> addFavoriteExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Exercise ID")
            @PathVariable Integer exerciseId) {
        favoriteService.addFavoriteExercise(userDetails.getUsername(), exerciseId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/exercises/favorites/{exerciseId}")
    @Operation(summary = "Remove exercise from favorites", description = "Remove an exercise from user's favorites")
    public ResponseEntity<Void> removeFavoriteExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Exercise ID")
            @PathVariable Integer exerciseId) {
        favoriteService.removeFavoriteExercise(userDetails.getUsername(), exerciseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exercises/{exerciseId}/is-favorite")
    @Operation(summary = "Check if exercise is favorite", description = "Check if an exercise is in user's favorites")
    public ResponseEntity<Boolean> isExerciseFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Exercise ID")
            @PathVariable Integer exerciseId) {
        return ResponseEntity.ok(favoriteService.isExerciseFavorite(userDetails.getUsername(), exerciseId));
    }

    // ========== RECENTLY USED ENDPOINTS ==========

    @GetMapping("/exercises/recent")
    @Operation(summary = "Get recently used exercises", description = "Get list of exercises recently used by the user")
    public ResponseEntity<List<ExerciseResponse>> getRecentExercises(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Limit number of results (default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(recentService.getRecentExercises(userDetails.getUsername(), limit));
    }

    // ========== CUSTOM EXERCISE ENDPOINTS ==========

    @PostMapping("/exercises/custom")
    @Operation(summary = "Create custom exercise", description = "Create a custom exercise item for the authenticated user")
    public ResponseEntity<ExerciseResponse> createCustomExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomExerciseRequest request) {
        ExerciseResponse exercise = exerciseLogService.createCustomExercise(userDetails.getUsername(), request);
        return ResponseEntity.ok(exercise);
    }

    @GetMapping("/exercises/my-custom")
    @Operation(summary = "Get custom exercises", description = "Get list of custom exercises created by the user")
    public ResponseEntity<List<ExerciseResponse>> getCustomExercises(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(exerciseLogService.getCustomExercises(userDetails.getUsername()));
    }

    @PutMapping("/exercises/custom/{exerciseId}")
    @Operation(summary = "Update custom exercise", description = "Update a custom exercise item created by the user")
    public ResponseEntity<ExerciseResponse> updateCustomExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Exercise ID")
            @PathVariable Integer exerciseId,
            @Valid @RequestBody CustomExerciseRequest request) {
        ExerciseResponse exercise = exerciseLogService.updateCustomExercise(userDetails.getUsername(), exerciseId, request);
        return ResponseEntity.ok(exercise);
    }

    @DeleteMapping("/exercises/custom/{exerciseId}")
    @Operation(summary = "Delete custom exercise", description = "Delete a custom exercise item created by the user")
    public ResponseEntity<Void> deleteCustomExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Exercise ID")
            @PathVariable Integer exerciseId) {
        exerciseLogService.deleteCustomExercise(userDetails.getUsername(), exerciseId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/exercises/check-name")
    @Operation(summary = "Check if exercise name exists", description = "Check if an exercise name already exists (for validation)")
    public ResponseEntity<Boolean> checkExerciseNameExists(
            @Parameter(description = "Exercise name to check")
            @RequestParam String name) {
        return ResponseEntity.ok(exerciseLogService.checkExerciseNameExists(name));
    }
}
