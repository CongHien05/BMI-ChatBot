package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.request.MeasurementRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.MeasurementResponse;
import vn.vku.udn.hienpc.bmichatbot.service.BodyMeasurementService;

import java.util.Optional;

@RestController
@RequestMapping("/api/measurements")
@CrossOrigin(origins = "*")
public class BodyMeasurementApiController {

    private final BodyMeasurementService bodyMeasurementService;

    public BodyMeasurementApiController(BodyMeasurementService bodyMeasurementService) {
        this.bodyMeasurementService = bodyMeasurementService;
    }

    @PostMapping
    @Operation(summary = "Create a new body measurement entry",
            description = "Stores the latest weight/height for the authenticated user")
    public ResponseEntity<MeasurementResponse> createMeasurement(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MeasurementRequest request) {

        MeasurementResponse response =
                bodyMeasurementService.createMeasurement(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest body measurement",
            description = "Returns the last recorded weight/height, if any")
    public ResponseEntity<MeasurementResponse> getLatestMeasurement(
            @AuthenticationPrincipal UserDetails userDetails) {

        Optional<MeasurementResponse> response =
                bodyMeasurementService.getLatestMeasurement(userDetails.getUsername());

        return response.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}

