package vn.vku.udn.hienpc.bmichatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.vku.udn.hienpc.bmichatbot.dto.response.DashboardSummary;
import vn.vku.udn.hienpc.bmichatbot.entity.BodyMeasurement;
import vn.vku.udn.hienpc.bmichatbot.entity.Food;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserFoodLog;
import vn.vku.udn.hienpc.bmichatbot.repository.BodyMeasurementRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserFoodLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BodyMeasurementRepository bodyMeasurementRepository;

    @Mock
    private UserFoodLogRepository userFoodLogRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSummary_shouldComputeBmiAndCalories() {
        User user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        BodyMeasurement bm = new BodyMeasurement();
        bm.setWeightKg(new BigDecimal("70"));
        bm.setHeightCm(new BigDecimal("170"));
        when(bodyMeasurementRepository.findByUserUserIdOrderByDateRecordedDesc(1))
                .thenReturn(List.of(bm));

        Food food = new Food();
        food.setCaloriesPerUnit(100);
        UserFoodLog log = new UserFoodLog();
        log.setFood(food);
        log.setQuantity(new BigDecimal("2"));
        log.setDateEaten(LocalDateTime.now());

        when(userFoodLogRepository.findByUserUserIdAndDateEatenBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(log));

        DashboardSummary summary = dashboardService.getSummary("test@example.com");

        assertEquals(70.0, summary.getCurrentWeight());
        // BMI ~ 24.22
        double bmi = summary.getBmi();
        assertEquals(true, bmi > 24.0 && bmi < 24.3);
        assertEquals(200, summary.getTotalCaloriesToday());
    }
}


