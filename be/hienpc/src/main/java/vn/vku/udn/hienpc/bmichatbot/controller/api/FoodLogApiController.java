package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.request.CustomFoodRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.request.FoodLogRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodLogHistoryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodResponse;
import vn.vku.udn.hienpc.bmichatbot.service.FoodLogService;
import vn.vku.udn.hienpc.bmichatbot.service.FavoriteService;
import vn.vku.udn.hienpc.bmichatbot.service.RecentService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FoodLogApiController {

    private final FoodLogService foodLogService;
    private final FavoriteService favoriteService;
    private final RecentService recentService;

    public FoodLogApiController(FoodLogService foodLogService, FavoriteService favoriteService, RecentService recentService) {
        this.foodLogService = foodLogService;
        this.favoriteService = favoriteService;
        this.recentService = recentService;
    }

    @GetMapping("/foods")
    @Operation(summary = "Get all foods", description = "Return list of foods that user can select when logging meals (includes public foods and user's custom foods)")
    public ResponseEntity<List<FoodResponse>> getFoods(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Search query (optional)")
            @RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(foodLogService.searchFoods(userDetails.getUsername(), q));
        }
        return ResponseEntity.ok(foodLogService.getAllFoods(userDetails.getUsername()));
    }

    @PostMapping("/logs/food")
    @Operation(summary = "Log food", description = "Create a food log entry for the authenticated user")
    public ResponseEntity<Void> logFood(@AuthenticationPrincipal UserDetails userDetails,
                                        @Valid @RequestBody FoodLogRequest request) {
        foodLogService.logFood(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/logs/food/history")
    @Operation(summary = "Get food log history", description = "Get food logs filtered by date range")
    public ResponseEntity<List<FoodLogHistoryResponse>> getFoodLogHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        List<FoodLogHistoryResponse> history = foodLogService.getFoodLogHistory(
                userDetails.getUsername(), from, to);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/logs/food/{logId}")
    @Operation(summary = "Update food log", description = "Update an existing food log entry")
    public ResponseEntity<Void> updateFoodLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Log ID")
            @PathVariable Integer logId,
            @Valid @RequestBody FoodLogRequest request) {
        foodLogService.updateFoodLog(userDetails.getUsername(), logId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logs/food/{logId}")
    @Operation(summary = "Delete food log", description = "Delete a food log entry")
    public ResponseEntity<Void> deleteFoodLog(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Log ID")
            @PathVariable Integer logId) {
        foodLogService.deleteFoodLog(userDetails.getUsername(), logId);
        return ResponseEntity.ok().build();
    }

    // ========== FAVORITES ENDPOINTS ==========

    @GetMapping("/foods/favorites")
    @Operation(summary = "Get favorite foods", description = "Get list of user's favorite foods")
    public ResponseEntity<List<FoodResponse>> getFavoriteFoods(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(favoriteService.getFavoriteFoods(userDetails.getUsername()));
    }

    @PostMapping("/foods/favorites/{foodId}")
    @Operation(summary = "Add food to favorites", description = "Add a food to user's favorites")
    public ResponseEntity<Void> addFavoriteFood(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Food ID")
            @PathVariable Integer foodId) {
        favoriteService.addFavoriteFood(userDetails.getUsername(), foodId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/foods/favorites/{foodId}")
    @Operation(summary = "Remove food from favorites", description = "Remove a food from user's favorites")
    public ResponseEntity<Void> removeFavoriteFood(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Food ID")
            @PathVariable Integer foodId) {
        favoriteService.removeFavoriteFood(userDetails.getUsername(), foodId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/foods/{foodId}/is-favorite")
    @Operation(summary = "Check if food is favorite", description = "Check if a food is in user's favorites")
    public ResponseEntity<Boolean> isFoodFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Food ID")
            @PathVariable Integer foodId) {
        return ResponseEntity.ok(favoriteService.isFoodFavorite(userDetails.getUsername(), foodId));
    }

    // ========== RECENTLY USED ENDPOINTS ==========

    @GetMapping("/foods/recent")
    @Operation(summary = "Get recently used foods", description = "Get list of foods recently used by the user")
    public ResponseEntity<List<FoodResponse>> getRecentFoods(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Limit number of results (default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(recentService.getRecentFoods(userDetails.getUsername(), limit));
    }

    // ========== CUSTOM FOOD ENDPOINTS ==========

    @PostMapping("/foods/custom")
    @Operation(summary = "Create custom food", description = "Create a custom food item for the authenticated user")
    public ResponseEntity<FoodResponse> createCustomFood(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomFoodRequest request) {
        FoodResponse food = foodLogService.createCustomFood(userDetails.getUsername(), request);
        return ResponseEntity.ok(food);
    }

    @GetMapping("/foods/my-custom")
    @Operation(summary = "Get custom foods", description = "Get list of custom foods created by the user")
    public ResponseEntity<List<FoodResponse>> getCustomFoods(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(foodLogService.getCustomFoods(userDetails.getUsername()));
    }

    @PutMapping("/foods/custom/{foodId}")
    @Operation(summary = "Update custom food", description = "Update a custom food item created by the user")
    public ResponseEntity<FoodResponse> updateCustomFood(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Food ID")
            @PathVariable Integer foodId,
            @Valid @RequestBody CustomFoodRequest request) {
        FoodResponse food = foodLogService.updateCustomFood(userDetails.getUsername(), foodId, request);
        return ResponseEntity.ok(food);
    }

    @DeleteMapping("/foods/custom/{foodId}")
    @Operation(summary = "Delete custom food", description = "Delete a custom food item created by the user")
    public ResponseEntity<Void> deleteCustomFood(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Food ID")
            @PathVariable Integer foodId) {
        foodLogService.deleteCustomFood(userDetails.getUsername(), foodId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/foods/check-name")
    @Operation(summary = "Check if food name exists", description = "Check if a food name already exists (for validation)")
    public ResponseEntity<Boolean> checkFoodNameExists(
            @Parameter(description = "Food name to check")
            @RequestParam String name) {
        return ResponseEntity.ok(foodLogService.checkFoodNameExists(name));
    }
}
