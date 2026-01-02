package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vku.udn.hienpc.bmichatbot.dto.request.CustomFoodRequest;
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
    private final RecentService recentService;

    public FoodLogService(FoodRepository foodRepository,
                          UserRepository userRepository,
                          UserFoodLogRepository userFoodLogRepository,
                          StreakService streakService,
                          RecentService recentService) {
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
        this.userFoodLogRepository = userFoodLogRepository;
        this.streakService = streakService;
        this.recentService = recentService;
    }

    public List<FoodResponse> getAllFoods(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        // Get public foods: foods without createdByUser (admin created or old foods)
        // OR foods with isPublic = true
        List<Food> publicFoods = foodRepository.findAll().stream()
                .filter(f -> f.getCreatedByUser() == null || 
                            (f.getIsPublic() != null && f.getIsPublic()))
                .collect(Collectors.toList());
        
        List<Food> customFoods = foodRepository.findCustomFoodsByUser(user.getUserId());
        
        // Combine and convert to response
        List<Food> allFoods = new java.util.ArrayList<>(publicFoods);
        allFoods.addAll(customFoods);
        
        return allFoods.stream()
                .map(f -> new FoodResponse(
                        f.getFoodId(),
                        f.getFoodName(),
                        f.getServingUnit(),
                        f.getCaloriesPerUnit()))
                .collect(Collectors.toList());
    }
    
    public List<FoodResponse> searchFoods(String userEmail, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllFoods(userEmail);
        }
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        // Search in public foods: foods without createdByUser (admin created or old foods)
        // OR foods with isPublic = true
        List<Food> publicFoods = foodRepository.searchByName(query.trim()).stream()
                .filter(f -> f.getCreatedByUser() == null || 
                            (f.getIsPublic() != null && f.getIsPublic()))
                .collect(Collectors.toList());
        
        // Search in custom foods of this user
        List<Food> customFoods = foodRepository.findCustomFoodsByUser(user.getUserId()).stream()
                .filter(f -> f.getFoodName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        
        // Combine and convert to response
        List<Food> allFoods = new java.util.ArrayList<>(publicFoods);
        allFoods.addAll(customFoods);
        
        return allFoods.stream()
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
        
        // Update recently used foods
        recentService.updateRecentFood(userEmail, request.getFoodId());
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
    
    // ========== CUSTOM FOOD METHODS ==========
    
    @Transactional
    public FoodResponse createCustomFood(String userEmail, CustomFoodRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        Food food = new Food();
        food.setFoodName(request.getFoodName().trim());
        food.setServingUnit(request.getServingUnit().trim());
        food.setCaloriesPerUnit(request.getCaloriesPerUnit());
        food.setCreatedByUser(user);
        food.setCreatedByAdmin(null);
        food.setIsPublic(false); // User-created foods are private
        
        Food savedFood = foodRepository.save(food);
        
        return new FoodResponse(
                savedFood.getFoodId(),
                savedFood.getFoodName(),
                savedFood.getServingUnit(),
                savedFood.getCaloriesPerUnit()
        );
    }
    
    public List<FoodResponse> getCustomFoods(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        return foodRepository.findCustomFoodsByUser(user.getUserId()).stream()
                .map(f -> new FoodResponse(
                        f.getFoodId(),
                        f.getFoodName(),
                        f.getServingUnit(),
                        f.getCaloriesPerUnit()))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public FoodResponse updateCustomFood(String userEmail, Integer foodId, CustomFoodRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new IllegalArgumentException("Food not found with id: " + foodId));
        
        // Verify food belongs to user
        if (food.getCreatedByUser() == null || !food.getCreatedByUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Food does not belong to user or is not a custom food");
        }
        
        food.setFoodName(request.getFoodName().trim());
        food.setServingUnit(request.getServingUnit().trim());
        food.setCaloriesPerUnit(request.getCaloriesPerUnit());
        
        Food updatedFood = foodRepository.save(food);
        
        return new FoodResponse(
                updatedFood.getFoodId(),
                updatedFood.getFoodName(),
                updatedFood.getServingUnit(),
                updatedFood.getCaloriesPerUnit()
        );
    }
    
    @Transactional
    public void deleteCustomFood(String userEmail, Integer foodId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new IllegalArgumentException("Food not found with id: " + foodId));
        
        // Verify food belongs to user
        if (food.getCreatedByUser() == null || !food.getCreatedByUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Food does not belong to user or is not a custom food");
        }
        
        foodRepository.delete(food);
    }
    
    public boolean checkFoodNameExists(String foodName) {
        if (foodName == null || foodName.trim().isEmpty()) {
            return false;
        }
        return foodRepository.existsByFoodNameIgnoreCase(foodName.trim());
    }
}
