package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import vn.vku.udn.hienpc.bmichatbot.dto.request.FoodLogRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodLogHistoryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.Food;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserFoodLog;
import vn.vku.udn.hienpc.bmichatbot.repository.FoodRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserFoodLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoodLogService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    private final UserFoodLogRepository userFoodLogRepository;
    private final StreakService streakService;

    public FoodLogService(FoodRepository foodRepository,
                          UserRepository userRepository,
                          UserFoodLogRepository userFoodLogRepository,
                          StreakService streakService) {
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.userFoodLogRepository = userFoodLogRepository;
        this.streakService = streakService;
    }

    public List<FoodResponse> getAllFoods() {
        return foodRepository.findAll().stream()
                .map(f -> new FoodResponse(
                        f.getFoodId(),
                        f.getFoodName(),
                        f.getServingUnit(),
                        f.getCaloriesPerUnit()))
                .collect(Collectors.toList());
    }
    
    public List<FoodResponse> searchFoods(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllFoods();
        }
        
        return foodRepository.searchByName(query.trim()).stream()
                .map(f -> new FoodResponse(
                        f.getFoodId(),
                        f.getFoodName(),
                        f.getServingUnit(),
                        f.getCaloriesPerUnit()))
                .collect(Collectors.toList());
    }

    public void logFood(String userEmail, FoodLogRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("Food not found with id: " + request.getFoodId()));

        UserFoodLog log = new UserFoodLog();
        log.setUser(user);
        log.setFood(food);
        log.setDateEaten(LocalDateTime.now());
        log.setMealType(request.getMealType());
        log.setQuantity(request.getQuantity());

        userFoodLogRepository.save(log);
        
        // Update streak after successful log
        streakService.updateStreak(user.getUserId());
    }

    public List<FoodLogHistoryResponse> getFoodLogHistory(String userEmail, LocalDate from, LocalDate to) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        // Default to last 30 days if not specified
        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(29);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<UserFoodLog> logs = userFoodLogRepository
                .findByUserUserIdAndDateEatenBetween(user.getUserId(), startDateTime, endDateTime);

        return logs.stream()
                .map(log -> {
                    Food food = log.getFood();
                    Integer caloriesPerUnit = (food != null && food.getCaloriesPerUnit() != null) 
                            ? food.getCaloriesPerUnit() : 0;
                    Double quantity = (log.getQuantity() != null) ? log.getQuantity().doubleValue() : 0.0;
                    Integer totalCalories = (int) Math.round(quantity * caloriesPerUnit);

                    return new FoodLogHistoryResponse(
                            log.getLogId(),
                            food != null ? food.getFoodId() : null,
                            food != null ? food.getFoodName() : "Unknown",
                            quantity,
                            food != null ? food.getServingUnit() : "",
                            caloriesPerUnit,
                            totalCalories,
                            log.getMealType(),
                            log.getDateEaten()
                    );
                })
                .collect(Collectors.toList());
    }

    public void updateFoodLog(String userEmail, Integer logId, FoodLogRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        UserFoodLog log = userFoodLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Food log not found with id: " + logId));

        // Verify log belongs to user
        if (!log.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Food log does not belong to user");
        }

        // Only allow editing logs from today
        LocalDate logDate = log.getDateEaten().toLocalDate();
        LocalDate today = LocalDate.now();
        if (!logDate.equals(today)) {
            throw new IllegalArgumentException("Chỉ có thể chỉnh sửa log trong ngày hiện tại");
        }

        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("Food not found with id: " + request.getFoodId()));

        log.setFood(food);
        log.setQuantity(request.getQuantity());
        log.setMealType(request.getMealType());

        userFoodLogRepository.save(log);
        
        // Update streak after successful update
        streakService.updateStreak(user.getUserId());
    }

    public void deleteFoodLog(String userEmail, Integer logId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        UserFoodLog log = userFoodLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Food log not found with id: " + logId));

        // Verify log belongs to user
        if (!log.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Food log does not belong to user");
        }

        // Only allow deleting logs from today
        LocalDate logDate = log.getDateEaten().toLocalDate();
        LocalDate today = LocalDate.now();
        if (!logDate.equals(today)) {
            throw new IllegalArgumentException("Chỉ có thể xóa log trong ngày hiện tại");
        }

        userFoodLogRepository.delete(log);
        
        // Update streak after successful delete
        streakService.updateStreak(user.getUserId());
    }
}
