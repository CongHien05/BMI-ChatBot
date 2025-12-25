package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.request.FoodLogRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodLogHistoryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodResponse;
import vn.vku.udn.hienpc.bmichatbot.service.FoodLogService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FoodLogApiController {

    private final FoodLogService foodLogService;

    public FoodLogApiController(FoodLogService foodLogService) {
        this.foodLogService = foodLogService;
    }

    @GetMapping("/foods")
    @Operation(summary = "Get all foods", description = "Return list of foods that user can select when logging meals")
    public ResponseEntity<List<FoodResponse>> getFoods(
            @Parameter(description = "Search query (optional)")
            @RequestParam(required = false) String q) {
        if (q != null && !q.trim().isEmpty()) {
            return ResponseEntity.ok(foodLogService.searchFoods(q));
        }
        return ResponseEntity.ok(foodLogService.getAllFoods());
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
}
