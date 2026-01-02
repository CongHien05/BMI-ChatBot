package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.response.AdminDashboardResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.AuditLogResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.UserDetailResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.UserSummaryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.request.CustomExerciseRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.request.CustomFoodRequest;
import vn.vku.udn.hienpc.bmichatbot.entity.AuditLog;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.service.AdminService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@Tag(name = "Admin API", description = "Admin management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final AdminService adminService;

    public AdminApiController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get admin dashboard statistics", description = "Get overview statistics for admin dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(adminService.getDashboardStats(userDetails.getUsername()));
    }

    @GetMapping("/foods")
    @Operation(summary = "Get all foods (admin)", description = "Get all foods including public and custom foods")
    public ResponseEntity<List<FoodResponse>> getAllFoods(
            @Parameter(description = "Search query (optional)")
            @RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(adminService.searchFoods(q));
        }
        return ResponseEntity.ok(adminService.getAllFoods());
    }

    @PostMapping("/foods")
    @Operation(summary = "Create food (admin)", description = "Create a new public food item")
    public ResponseEntity<FoodResponse> createFood(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomFoodRequest request) {
        FoodResponse food = adminService.createFood(userDetails.getUsername(), request);
        return ResponseEntity.ok(food);
    }

    @PutMapping("/foods/{foodId}")
    @Operation(summary = "Update food (admin)", description = "Update an existing food item")
    public ResponseEntity<FoodResponse> updateFood(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Food ID")
            @PathVariable Integer foodId,
            @Valid @RequestBody CustomFoodRequest request) {
        FoodResponse food = adminService.updateFood(userDetails.getUsername(), foodId, request);
        return ResponseEntity.ok(food);
    }

    @DeleteMapping("/foods/{foodId}")
    @Operation(summary = "Delete food (admin)", description = "Delete a food item")
    public ResponseEntity<Void> deleteFood(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Food ID")
            @PathVariable Integer foodId) {
        adminService.deleteFood(userDetails.getUsername(), foodId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exercises")
    @Operation(summary = "Get all exercises (admin)", description = "Get all exercises including public and custom exercises")
    public ResponseEntity<List<ExerciseResponse>> getAllExercises(
            @Parameter(description = "Search query (optional)")
            @RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(adminService.searchExercises(q));
        }
        return ResponseEntity.ok(adminService.getAllExercises());
    }

    @PostMapping("/exercises")
    @Operation(summary = "Create exercise (admin)", description = "Create a new public exercise item")
    public ResponseEntity<ExerciseResponse> createExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomExerciseRequest request) {
        ExerciseResponse exercise = adminService.createExercise(userDetails.getUsername(), request);
        return ResponseEntity.ok(exercise);
    }

    @PutMapping("/exercises/{exerciseId}")
    @Operation(summary = "Update exercise (admin)", description = "Update an existing exercise item")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Exercise ID")
            @PathVariable Integer exerciseId,
            @Valid @RequestBody CustomExerciseRequest request) {
        ExerciseResponse exercise = adminService.updateExercise(userDetails.getUsername(), exerciseId, request);
        return ResponseEntity.ok(exercise);
    }

    @DeleteMapping("/exercises/{exerciseId}")
    @Operation(summary = "Delete exercise (admin)", description = "Delete an exercise item")
    public ResponseEntity<Void> deleteExercise(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Exercise ID")
            @PathVariable Integer exerciseId) {
        adminService.deleteExercise(userDetails.getUsername(), exerciseId);
        return ResponseEntity.ok().build();
    }

    // ========== USER MANAGEMENT API ==========

    @GetMapping("/users")
    @Operation(summary = "Get all users (admin)", description = "Get paginated list of all users")
    public ResponseEntity<List<UserSummaryResponse>> getAllUsers(
            @Parameter(description = "Search query (optional)")
            @RequestParam(required = false) String q,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(q, page, size));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user details (admin)", description = "Get detailed information about a specific user")
    public ResponseEntity<UserDetailResponse> getUserDetails(
            @Parameter(description = "User ID")
            @PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.getUserDetails(userId));
    }

    // ========== AUDIT LOGS API ==========

    @GetMapping("/audit-logs")
    @Operation(summary = "Get audit logs (admin)", description = "Get paginated audit logs for admin actions")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogs(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(adminService.getAuditLogs(page, size));
    }

    @GetMapping("/audit-logs/recent")
    @Operation(summary = "Get recent audit logs (admin)", description = "Get most recent audit logs")
    public ResponseEntity<List<AuditLogResponse>> getRecentAuditLogs(
            @Parameter(description = "Number of recent logs to retrieve")
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(adminService.getRecentAuditLogs(limit));
    }
}


