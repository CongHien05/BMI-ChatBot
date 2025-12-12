package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vku.udn.hienpc.bmichatbot.dto.response.DashboardSummary;
import vn.vku.udn.hienpc.bmichatbot.dto.response.MonthlySummaryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.TrendAnalysisResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.WeeklySummaryResponse;
import vn.vku.udn.hienpc.bmichatbot.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@Tag(name = "Dashboard", description = "Dashboard and analytics APIs")
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary", description = "Return current weight, BMI and total calories for today")
    public ResponseEntity<DashboardSummary> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        DashboardSummary summary = dashboardService.getSummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/weekly-summary")
    @Operation(summary = "Get weekly summary", description = "Return daily summaries for the last 7 days with charts data")
    public ResponseEntity<WeeklySummaryResponse> getWeeklySummary(@AuthenticationPrincipal UserDetails userDetails) {
        WeeklySummaryResponse summary = dashboardService.getWeeklySummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/monthly-summary")
    @Operation(summary = "Get monthly summary", description = "Return daily summaries for the last 30 days with charts data")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(@AuthenticationPrincipal UserDetails userDetails) {
        MonthlySummaryResponse summary = dashboardService.getMonthlySummary(userDetails.getUsername());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/trends")
    @Operation(summary = "Get trend analysis", description = "Analyze weight, calories, and activity trends with insights")
    public ResponseEntity<TrendAnalysisResponse> getTrendAnalysis(@AuthenticationPrincipal UserDetails userDetails) {
        TrendAnalysisResponse analysis = dashboardService.getTrendAnalysis(userDetails.getUsername());
        return ResponseEntity.ok(analysis);
    }
}

