package vn.vku.udn.hienpc.bmichatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.vku.udn.hienpc.bmichatbot.dto.response.DashboardSummary;
import vn.vku.udn.hienpc.bmichatbot.dto.response.MonthlySummaryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.TrendAnalysisResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.WeeklySummaryResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.*;
import vn.vku.udn.hienpc.bmichatbot.repository.BodyMeasurementRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserExerciseLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserFoodLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BodyMeasurementRepository bodyMeasurementRepository;

    @Mock
    private UserFoodLogRepository userFoodLogRepository;

    @Mock
    private UserExerciseLogRepository userExerciseLogRepository;

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

    @Test
    void getWeeklySummary_shouldReturnSevenDaysSummary() {
        // Arrange
        User user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Create measurements for 7 days
        List<BodyMeasurement> measurements = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            BodyMeasurement bm = new BodyMeasurement();
            bm.setUser(user);
            bm.setWeightKg(new BigDecimal(70 - i * 0.5)); // Weight decreasing
            bm.setHeightCm(new BigDecimal("170"));
            bm.setDateRecorded(LocalDate.now().minusDays(6 - i));
            measurements.add(bm);
        }
        when(bodyMeasurementRepository.findByUserUserIdOrderByDateRecordedDesc(1))
                .thenReturn(measurements);

        // Create food logs
        List<UserFoodLog> foodLogs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Food food = new Food();
            food.setCaloriesPerUnit(100);
            UserFoodLog log = new UserFoodLog();
            log.setUser(user);
            log.setFood(food);
            log.setQuantity(new BigDecimal("2"));
            log.setDateEaten(LocalDate.now().minusDays(6 - i).atStartOfDay());
            foodLogs.add(log);
        }
        when(userFoodLogRepository.findByUserUserIdAndDateEatenBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(foodLogs);

        // Create exercise logs
        List<UserExerciseLog> exerciseLogs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Exercise exercise = new Exercise();
            exercise.setCaloriesBurnedPerHour(300); // 5 cal/min
            UserExerciseLog log = new UserExerciseLog();
            log.setUser(user);
            log.setExercise(exercise);
            log.setDurationMinutes(30);
            log.setDateExercised(LocalDate.now().minusDays(6 - i).atStartOfDay());
            exerciseLogs.add(log);
        }
        when(userExerciseLogRepository.findByUserUserIdAndDateExercisedBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(exerciseLogs);

        // Act
        WeeklySummaryResponse summary = dashboardService.getWeeklySummary("test@example.com");

        // Assert
        assertNotNull(summary);
        assertEquals(7, summary.getDailySummaries().size());
        assertNotNull(summary.getAverageCalories());
        assertNotNull(summary.getAverageWeight());
        assertNotNull(summary.getWeightChange());
        assertTrue(summary.getWeightChange() < 0); // Weight decreasing
        assertEquals(7, summary.getTotalFoodLogs());
        assertEquals(7, summary.getTotalExerciseLogs());
    }

    @Test
    void getMonthlySummary_shouldReturnThirtyDaysSummary() {
        // Arrange
        User user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Create measurements for 30 days
        // Note: findByUserUserIdOrderByDateRecordedDesc returns DESC (newest first)
        // So we create measurements in reverse order: newest (i=0) first, oldest (i=29) last
        // Weight should decrease from 75 (earliest) to 69.2 (latest)
        List<BodyMeasurement> measurements = new ArrayList<>();
        for (int i = 29; i >= 0; i--) { // Reverse order to match DESC
            BodyMeasurement bm = new BodyMeasurement();
            bm.setUser(user);
            // Weight formula: 75 - (29 - i) * 0.2
            // i=29 (earliest): 75 - 0*0.2 = 75
            // i=0 (latest): 75 - 29*0.2 = 69.2
            bm.setWeightKg(new BigDecimal(75 - (29 - i) * 0.2)); // Gradual weight loss: 75 -> 69.2
            bm.setHeightCm(new BigDecimal("170"));
            bm.setDateRecorded(LocalDate.now().minusDays(i));
            measurements.add(bm);
        }
        when(bodyMeasurementRepository.findByUserUserIdOrderByDateRecordedDesc(1))
                .thenReturn(measurements);

        // Create food logs
        List<UserFoodLog> foodLogs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Food food = new Food();
            food.setCaloriesPerUnit(150);
            UserFoodLog log = new UserFoodLog();
            log.setUser(user);
            log.setFood(food);
            log.setQuantity(new BigDecimal("2"));
            log.setDateEaten(LocalDate.now().minusDays(29 - i).atStartOfDay());
            foodLogs.add(log);
        }
        when(userFoodLogRepository.findByUserUserIdAndDateEatenBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(foodLogs);

        // Create exercise logs
        List<UserExerciseLog> exerciseLogs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Exercise exercise = new Exercise();
            exercise.setCaloriesBurnedPerHour(360); // 6 cal/min
            UserExerciseLog log = new UserExerciseLog();
            log.setUser(user);
            log.setExercise(exercise);
            log.setDurationMinutes(45);
            log.setDateExercised(LocalDate.now().minusDays(29 - i).atStartOfDay());
            exerciseLogs.add(log);
        }
        when(userExerciseLogRepository.findByUserUserIdAndDateExercisedBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(exerciseLogs);

        // Act
        MonthlySummaryResponse summary = dashboardService.getMonthlySummary("test@example.com");

        // Assert
        assertNotNull(summary);
        assertEquals(30, summary.getDailySummaries().size());
        assertNotNull(summary.getAverageCalories());
        assertNotNull(summary.getAverageWeight());
        assertNotNull(summary.getWeightChange());
        // Weight change: from 75 (earliest, startDate) to 69.2 (latest, endDate) = -5.8
        // Note: weightChange = weights.get(size-1) - weights.get(0) where weights are in date order
        assertTrue(summary.getWeightChange() < 0, 
            "Weight should decrease from 75 to 69.2. Actual weight change: " + summary.getWeightChange());
        assertEquals(30, summary.getTotalFoodLogs());
        assertEquals(30, summary.getTotalExerciseLogs());
        // BMI change: weight decreases from 75 to 69.2, height 170cm
        // BMI start: 75 / (1.70^2) ≈ 25.95
        // BMI end: 69.2 / (1.70^2) ≈ 23.95
        // BMI change should be negative (decreasing)
        // Note: calculateBmiChange uses latest - earliest, so if weight decreases, BMI change is negative
        assertNotNull(summary.getBmiChange(), "BMI change should be calculated");
        // Since weight decreases (75 -> 69.2), BMI should also decrease
        // But the calculation is: latestBmi - earliestBmi
        // With DESC order: latest = 69.2, earliest = 75
        // So: BMI change = (69.2/1.70^2) - (75/1.70^2) = negative
        assertTrue(summary.getBmiChange() < 0, 
            "BMI should decrease when weight decreases. BMI change: " + summary.getBmiChange());
    }

    @Test
    void getTrendAnalysis_shouldAnalyzeTrend() {
        // Arrange
        User user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Create measurements showing weight gain (30 days for trend analysis)
        List<BodyMeasurement> measurements = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            BodyMeasurement bm = new BodyMeasurement();
            bm.setUser(user);
            bm.setWeightKg(new BigDecimal(65 + i * 0.3)); // Weight increasing
            bm.setHeightCm(new BigDecimal("170"));
            bm.setDateRecorded(LocalDate.now().minusDays(29 - i));
            measurements.add(bm);
        }
        when(bodyMeasurementRepository.findByUserUserIdOrderByDateRecordedDesc(1))
                .thenReturn(measurements);

        // Create food logs
        List<UserFoodLog> foodLogs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Food food = new Food();
            food.setCaloriesPerUnit(200);
            UserFoodLog log = new UserFoodLog();
            log.setUser(user);
            log.setFood(food);
            log.setQuantity(new BigDecimal("2"));
            log.setDateEaten(LocalDate.now().minusDays(29 - i).atStartOfDay());
            foodLogs.add(log);
        }
        when(userFoodLogRepository.findByUserUserIdAndDateEatenBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(foodLogs);

        // Create exercise logs
        List<UserExerciseLog> exerciseLogs = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Exercise exercise = new Exercise();
            exercise.setCaloriesBurnedPerHour(300);
            UserExerciseLog log = new UserExerciseLog();
            log.setUser(user);
            log.setExercise(exercise);
            log.setDurationMinutes(30);
            log.setDateExercised(LocalDate.now().minusDays(29 - i).atStartOfDay());
            exerciseLogs.add(log);
        }
        when(userExerciseLogRepository.findByUserUserIdAndDateExercisedBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(exerciseLogs);

        // Act
        TrendAnalysisResponse trend = dashboardService.getTrendAnalysis("test@example.com");

        // Assert
        assertNotNull(trend);
        assertEquals("GAINING", trend.getWeightTrend()); // Weight increasing
        assertNotNull(trend.getWeightChangeRate());
        assertTrue(trend.getWeightChangeRate() > 0); // Positive change rate
        assertNotNull(trend.getAvgWeeklyExercises());
    }

    @Test
    void getWeeklySummary_withNoData_shouldReturnEmptySummary() {
        // Arrange
        User user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        when(bodyMeasurementRepository.findByUserUserIdOrderByDateRecordedDesc(1))
                .thenReturn(List.of());
        when(userFoodLogRepository.findByUserUserIdAndDateEatenBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(userExerciseLogRepository.findByUserUserIdAndDateExercisedBetween(
                any(Integer.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // Act
        WeeklySummaryResponse summary = dashboardService.getWeeklySummary("test@example.com");

        // Assert
        assertNotNull(summary);
        assertEquals(7, summary.getDailySummaries().size()); // Still 7 days, just empty
        assertEquals(0, summary.getTotalFoodLogs());
        assertEquals(0, summary.getTotalExerciseLogs());
    }

    @Test
    void getSummary_withUserNotFound_shouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            dashboardService.getSummary("notfound@example.com");
        });
    }
}


