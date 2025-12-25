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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExerciseLogApiController {

    private final ExerciseLogService exerciseLogService;

    public ExerciseLogApiController(ExerciseLogService exerciseLogService) {
        this.exerciseLogService = exerciseLogService;
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
}
