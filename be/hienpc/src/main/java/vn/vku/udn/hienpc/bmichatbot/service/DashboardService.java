package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import vn.vku.udn.hienpc.bmichatbot.dto.response.*;
import vn.vku.udn.hienpc.bmichatbot.entity.BodyMeasurement;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserExerciseLog;
import vn.vku.udn.hienpc.bmichatbot.entity.UserFoodLog;
import vn.vku.udn.hienpc.bmichatbot.repository.BodyMeasurementRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserExerciseLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserFoodLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;
    private final UserFoodLogRepository userFoodLogRepository;
    private final UserExerciseLogRepository userExerciseLogRepository;

    public DashboardService(UserRepository userRepository,
                            BodyMeasurementRepository bodyMeasurementRepository,
                            UserFoodLogRepository userFoodLogRepository,
                            UserExerciseLogRepository userExerciseLogRepository) {
        this.userRepository = userRepository;
        this.bodyMeasurementRepository = bodyMeasurementRepository;
        this.userFoodLogRepository = userFoodLogRepository;
        this.userExerciseLogRepository = userExerciseLogRepository;
    }

    public DashboardSummary getSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        Double currentWeight = null;
        Double bmi = null;

        List<BodyMeasurement> measurements =
                bodyMeasurementRepository.findByUserUserIdOrderByDateRecordedDesc(user.getUserId());

        if (!measurements.isEmpty()) {
            BodyMeasurement latest = measurements.get(0);
            if (latest.getWeightKg() != null) {
                currentWeight = latest.getWeightKg().doubleValue();
            }
            if (latest.getWeightKg() != null && latest.getHeightCm() != null) {
                double w = latest.getWeightKg().doubleValue();
                double hMeters = latest.getHeightCm().doubleValue() / 100.0;
                if (hMeters > 0) {
                    bmi = w / (hMeters * hMeters);
                }
            }
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<UserFoodLog> foodLogsToday =
                userFoodLogRepository.findByUserUserIdAndDateEatenBetween(
                        user.getUserId(), startOfDay, endOfDay);

        int totalCaloriesToday = foodLogsToday.stream()
                .mapToInt(log -> {
                    if (log.getFood() == null || log.getFood().getCaloriesPerUnit() == null) {
                        return 0;
                    }
                    double qty = log.getQuantity() != null ? log.getQuantity().doubleValue() : 0.0;
                    return (int) Math.round(qty * log.getFood().getCaloriesPerUnit());
                })
                .sum();

        return new DashboardSummary(currentWeight, bmi, totalCaloriesToday);
    }

    /**
     * Get weekly summary - 7 ngày gần nhất
     */
    public WeeklySummaryResponse getWeeklySummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6); // 7 ngày bao gồm hôm nay

        return getSummaryForPeriod(user, sevenDaysAgo, today);
    }

    /**
     * Get monthly summary - 30 ngày gần nhất
     */
    public MonthlySummaryResponse getMonthlySummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29); // 30 ngày bao gồm hôm nay

        WeeklySummaryResponse weeklySummary = getSummaryForPeriod(user, thirtyDaysAgo, today);
        
        // Convert to MonthlySummaryResponse với BMI change
        Double bmiChange = calculateBmiChange(user, thirtyDaysAgo, today);
        
        return new MonthlySummaryResponse(
                weeklySummary.getDailySummaries(),
                weeklySummary.getAverageCalories(),
                weeklySummary.getAverageWeight(),
                weeklySummary.getWeightChange(),
                weeklySummary.getTotalFoodLogs(),
                weeklySummary.getTotalExerciseLogs(),
                bmiChange
        );
    }

    /**
     * Get trend analysis
     */
    public TrendAnalysisResponse getTrendAnalysis(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);

        WeeklySummaryResponse summary = getSummaryForPeriod(user, thirtyDaysAgo, today);

        // Analyze weight trend
        String weightTrend = "STABLE";
        Double weightChangeRate = 0.0;
        if (summary.getWeightChange() != null) {
            double changePerWeek = (summary.getWeightChange() / 30.0) * 7.0;
            weightChangeRate = changePerWeek;
            if (changePerWeek < -0.3) {
                weightTrend = "LOSING";
            } else if (changePerWeek > 0.3) {
                weightTrend = "GAINING";
            }
        }

        // Analyze calories trend
        String caloriesTrend = "STABLE";
        Double avgCalories = summary.getAverageCalories();

        // Analyze activity trend
        String activityTrend = "STABLE";
        Double avgWeeklyExercises = (summary.getTotalExerciseLogs() / 30.0) * 7.0;

        // Generate insight
        String insight = generateInsight(weightTrend, avgCalories, avgWeeklyExercises);

        // Check if on track (simplified logic)
        Boolean onTrack = isOnTrack(user, weightTrend);

        // Calculate days to goal (simplified)
        Integer daysToGoal = calculateDaysToGoal(user, weightChangeRate);

        return new TrendAnalysisResponse(
                weightTrend,
                weightChangeRate,
                caloriesTrend,
                avgCalories,
                activityTrend,
                avgWeeklyExercises,
                insight,
                onTrack,
                daysToGoal
        );
    }

    /**
     * Helper: Get summary for a date range
     */
    private WeeklySummaryResponse getSummaryForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        List<DailySummaryItem> dailySummaries = new ArrayList<>();

        // Get all measurements in period
        List<BodyMeasurement> measurements = bodyMeasurementRepository
                .findByUserUserIdOrderByDateRecordedDesc(user.getUserId())
                .stream()
                .filter(m -> !m.getDateRecorded().isBefore(startDate) 
                          && !m.getDateRecorded().isAfter(endDate))
                .collect(Collectors.toList());

        Map<LocalDate, Double> weightByDate = new HashMap<>();
        for (BodyMeasurement m : measurements) {
            LocalDate date = m.getDateRecorded();
            if (!weightByDate.containsKey(date) && m.getWeightKg() != null) {
                weightByDate.put(date, m.getWeightKg().doubleValue());
            }
        }

        // Get all logs in period
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        List<UserFoodLog> foodLogs = userFoodLogRepository
                .findByUserUserIdAndDateEatenBetween(user.getUserId(), startDateTime, endDateTime);

        List<UserExerciseLog> exerciseLogs = userExerciseLogRepository
                .findByUserUserIdAndDateExercisedBetween(user.getUserId(), startDateTime, endDateTime);

        // Group by date
        Map<LocalDate, List<UserFoodLog>> foodLogsByDate = foodLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getDateEaten().toLocalDate()));

        Map<LocalDate, List<UserExerciseLog>> exerciseLogsByDate = exerciseLogs.stream()
                .collect(Collectors.groupingBy(log -> log.getDateExercised().toLocalDate()));

        // Build daily summaries
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Double weight = weightByDate.get(date);

            List<UserFoodLog> dayFoodLogs = foodLogsByDate.getOrDefault(date, List.of());
            int totalCalories = dayFoodLogs.stream()
                    .mapToInt(log -> {
                        if (log.getFood() == null || log.getFood().getCaloriesPerUnit() == null) {
                            return 0;
                        }
                        double qty = log.getQuantity() != null ? log.getQuantity().doubleValue() : 0.0;
                        return (int) Math.round(qty * log.getFood().getCaloriesPerUnit());
                    })
                    .sum();

            List<UserExerciseLog> dayExerciseLogs = exerciseLogsByDate.getOrDefault(date, List.of());
            int caloriesBurned = dayExerciseLogs.stream()
                    .mapToInt(log -> {
                        if (log.getExercise() == null || log.getExercise().getCaloriesBurnedPerHour() == null) {
                            return 0;
                        }
                        double duration = log.getDurationMinutes() != null ? log.getDurationMinutes().doubleValue() : 0.0;
                        // Convert per hour to per minute
                        double caloriesPerMinute = log.getExercise().getCaloriesBurnedPerHour() / 60.0;
                        return (int) Math.round(duration * caloriesPerMinute);
                    })
                    .sum();

            int netCalories = totalCalories - caloriesBurned;

            dailySummaries.add(new DailySummaryItem(
                    date,
                    weight,
                    totalCalories,
                    caloriesBurned,
                    netCalories,
                    dayFoodLogs.size(),
                    dayExerciseLogs.size()
            ));
        }

        // Calculate aggregates
        Double averageCalories = dailySummaries.stream()
                .filter(d -> d.getTotalCalories() != null && d.getTotalCalories() > 0)
                .mapToInt(DailySummaryItem::getTotalCalories)
                .average()
                .orElse(0.0);

        List<Double> weights = dailySummaries.stream()
                .map(DailySummaryItem::getWeight)
                .filter(w -> w != null)
                .collect(Collectors.toList());

        Double averageWeight = weights.isEmpty() ? null : 
                weights.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        Double weightChange = null;
        if (weights.size() >= 2) {
            weightChange = weights.get(weights.size() - 1) - weights.get(0);
        }

        int totalFoodLogs = dailySummaries.stream()
                .mapToInt(DailySummaryItem::getFoodLogsCount)
                .sum();

        int totalExerciseLogs = dailySummaries.stream()
                .mapToInt(DailySummaryItem::getExerciseLogsCount)
                .sum();

        return new WeeklySummaryResponse(
                dailySummaries,
                averageCalories,
                averageWeight,
                weightChange,
                totalFoodLogs,
                totalExerciseLogs
        );
    }

    /**
     * Calculate BMI change over period
     */
    private Double calculateBmiChange(User user, LocalDate startDate, LocalDate endDate) {
        List<BodyMeasurement> measurements = bodyMeasurementRepository
                .findByUserUserIdOrderByDateRecordedDesc(user.getUserId())
                .stream()
                .filter(m -> !m.getDateRecorded().isBefore(startDate) 
                          && !m.getDateRecorded().isAfter(endDate))
                .collect(Collectors.toList());

        if (measurements.size() < 2) {
            return null;
        }

        // Get first and last measurements
        BodyMeasurement latest = measurements.get(0);
        BodyMeasurement earliest = measurements.get(measurements.size() - 1);

        Double latestBmi = calculateBmi(latest);
        Double earliestBmi = calculateBmi(earliest);

        if (latestBmi == null || earliestBmi == null) {
            return null;
        }

        return latestBmi - earliestBmi;
    }

    /**
     * Calculate BMI from measurement
     */
    private Double calculateBmi(BodyMeasurement measurement) {
        if (measurement.getWeightKg() == null || measurement.getHeightCm() == null) {
            return null;
        }
        double w = measurement.getWeightKg().doubleValue();
        double hMeters = measurement.getHeightCm().doubleValue() / 100.0;
        if (hMeters <= 0) {
            return null;
        }
        return w / (hMeters * hMeters);
    }

    /**
     * Generate insight message
     */
    private String generateInsight(String weightTrend, Double avgCalories, Double avgWeeklyExercises) {
        if ("LOSING".equals(weightTrend)) {
            return "Bạn đang giảm cân ổn định. Hãy duy trì chế độ hiện tại!";
        } else if ("GAINING".equals(weightTrend)) {
            return "Cân nặng của bạn đang tăng. Hãy kiểm tra lại calories intake.";
        } else {
            if (avgWeeklyExercises < 3) {
                return "Cân nặng ổn định. Hãy tăng thêm hoạt động thể chất để cải thiện sức khỏe.";
            }
            return "Bạn đang duy trì cân nặng tốt. Tiếp tục phát huy!";
        }
    }

    /**
     * Check if user is on track with their goal
     */
    private Boolean isOnTrack(User user, String weightTrend) {
        // Simplified logic - can be enhanced with UserProfile goal
        if ("LOSING".equals(weightTrend)) {
            return true; // Assume goal is to lose weight
        }
        return false;
    }

    /**
     * Calculate days to reach goal
     */
    private Integer calculateDaysToGoal(User user, Double weightChangeRate) {
        if (weightChangeRate == null || Math.abs(weightChangeRate) < 0.1) {
            return null; // No significant change
        }

        // Simplified: assume target is 5kg away
        double targetWeightDiff = 5.0;
        double weeksNeeded = targetWeightDiff / Math.abs(weightChangeRate);
        return (int) Math.round(weeksNeeded * 7);
    }
}


