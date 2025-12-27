package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.request.ExerciseLogRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseLogHistoryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseResponse;
import vn.vku.udn.hienpc.bmichatbot.service.ExerciseLogService;
import vn.vku.udn.hienpc.bmichatbot.service.FavoriteService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExerciseLogApiController {

    private final ExerciseLogService exerciseLogService;
    private final FavoriteService favoriteService;

    public ExerciseLogApiController(ExerciseLogService exerciseLogService, FavoriteService favoriteService) {
        this.exerciseLogService = exerciseLogService;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/exercises")
    @Operation(summary = "Get all exercises", description = "Return list of exercises for logging workouts")
    public ResponseEntity<List<ExerciseResponse>> getExercises(
            @Parameter(description = "Search query (optional)")
            @RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(exerciseLogService.searchExercises(q));
        }
        return ResponseEntity.ok(exerciseLogService.getAllExercises());
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
}
