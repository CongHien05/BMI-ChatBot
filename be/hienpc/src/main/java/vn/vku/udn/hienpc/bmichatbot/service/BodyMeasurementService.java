package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import vn.vku.udn.hienpc.bmichatbot.dto.request.MeasurementRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.MeasurementResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.BodyMeasurement;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.repository.BodyMeasurementRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BodyMeasurementService {

    private final UserRepository userRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;

    public BodyMeasurementService(UserRepository userRepository,
                                  BodyMeasurementRepository bodyMeasurementRepository) {
        this.userRepository = userRepository;
        this.bodyMeasurementRepository = bodyMeasurementRepository;
    }

    public MeasurementResponse createMeasurement(String userEmail, MeasurementRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        BodyMeasurement measurement = new BodyMeasurement();
        measurement.setUser(user);
        measurement.setWeightKg(request.getWeightKg());
        measurement.setHeightCm(request.getHeightCm());
        measurement.setDateRecorded(
                request.getDateRecorded() != null ? request.getDateRecorded() : LocalDate.now());

        BodyMeasurement saved = bodyMeasurementRepository.save(measurement);
        return mapToResponse(saved);
    }

    public Optional<MeasurementResponse> getLatestMeasurement(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        List<BodyMeasurement> measurements =
                bodyMeasurementRepository.findByUserUserIdOrderByDateRecordedDesc(user.getUserId());

        return measurements.stream()
                .findFirst()
                .map(this::mapToResponse);
    }

    private MeasurementResponse mapToResponse(BodyMeasurement measurement) {
        Double weight = measurement.getWeightKg() != null
                ? measurement.getWeightKg().doubleValue() : null;
        Double height = measurement.getHeightCm() != null
                ? measurement.getHeightCm().doubleValue() : null;
        return new MeasurementResponse(weight, height, measurement.getDateRecorded());
    }
}

