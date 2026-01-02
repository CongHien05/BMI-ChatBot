package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import vn.vku.udn.hienpc.bmichatbot.dto.request.CustomExerciseRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.request.CustomFoodRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.AdminDashboardResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.AuditLogResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.UserDetailResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.UserSummaryResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.AuditLog;
import vn.vku.udn.hienpc.bmichatbot.entity.Exercise;
import vn.vku.udn.hienpc.bmichatbot.entity.Food;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserRole;
import vn.vku.udn.hienpc.bmichatbot.entity.UserAchievement;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatbotRuleRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.AuditLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.ExerciseRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.FoodRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserAchievementRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserFoodLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserExerciseLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserProfileRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserFoodLogRepository userFoodLogRepository;
    private final UserExerciseLogRepository userExerciseLogRepository;
    private final ChatbotRuleRepository chatbotRuleRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserProfileRepository userProfileRepository;
    private final AuditLogService auditLogService;

    public AdminService(UserRepository userRepository,
                       FoodRepository foodRepository,
                       ExerciseRepository exerciseRepository,
                       UserFoodLogRepository userFoodLogRepository,
                       UserExerciseLogRepository userExerciseLogRepository,
                       ChatbotRuleRepository chatbotRuleRepository,
                       AuditLogRepository auditLogRepository,
                       UserAchievementRepository userAchievementRepository,
                       UserProfileRepository userProfileRepository,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.exerciseRepository = exerciseRepository;
        this.userFoodLogRepository = userFoodLogRepository;
        this.userExerciseLogRepository = userExerciseLogRepository;
        this.chatbotRuleRepository = chatbotRuleRepository;
        this.auditLogRepository = auditLogRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.userProfileRepository = userProfileRepository;
        this.auditLogService = auditLogService;
    }

    public AdminDashboardResponse getDashboardStats(String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminEmail));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        // Basic counts
        long totalUsers = userRepository.count();
        long totalFoods = foodRepository.count();
        long totalExercises = exerciseRepository.count();
        long totalFoodLogs = userFoodLogRepository.count();
        long totalExerciseLogs = userExerciseLogRepository.count();

        // Custom foods and exercises (created by users)
        long totalCustomFoods = foodRepository.countByCreatedByUserIsNotNull();
        long totalCustomExercises = exerciseRepository.countByCreatedByUserIsNotNull();

        // Chatbot and audit logs
        long totalChatbotRules = chatbotRuleRepository.count();
        long totalAuditLogs = auditLogRepository.count();

        // Date calculations
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        // Active users (avoid duplicate counting by using Set operations)
        long activeUsersToday = Math.max(
            userFoodLogRepository.countDistinctUsersByDate(today),
            userExerciseLogRepository.countDistinctUsersByDate(today)
        );

        long activeUsersThisWeek = Math.max(
            userFoodLogRepository.countDistinctUsersByDateRange(weekStart, today),
            userExerciseLogRepository.countDistinctUsersByDateRange(weekStart, today)
        );

        // New users this week - not available in current User entity
        long newUsersThisWeek = 0; // Placeholder

        // Today's logs
        long foodLogsToday = userFoodLogRepository.countByDate(today);
        long exerciseLogsToday = userExerciseLogRepository.countByDate(today);

        return new AdminDashboardResponse(
                totalUsers,
                totalFoods,
                totalExercises,
                totalFoodLogs,
                totalExerciseLogs,
                activeUsersToday,
                activeUsersThisWeek,
                totalCustomFoods,
                totalCustomExercises,
                totalChatbotRules,
                totalAuditLogs,
                newUsersThisWeek,
                foodLogsToday,
                exerciseLogsToday
        );
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
        return foodRepository.searchByName(query.trim()).stream()
                .map(f -> new FoodResponse(
                        f.getFoodId(),
                        f.getFoodName(),
                        f.getServingUnit(),
                        f.getCaloriesPerUnit()))
                .collect(Collectors.toList());
    }

    @Transactional
    public FoodResponse createFood(String adminEmail, CustomFoodRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminEmail));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        Food food = new Food();
        food.setFoodName(request.getFoodName().trim());
        food.setServingUnit(request.getServingUnit().trim());
        food.setCaloriesPerUnit(request.getCaloriesPerUnit());
        food.setCreatedByAdmin(admin);
        food.setCreatedByUser(null);
        food.setIsPublic(true);

        Food savedFood = foodRepository.save(food);

        auditLogService.logCreate(admin, "Food",
                String.valueOf(savedFood.getFoodId()),
                String.format("Name: %s, Unit: %s, Calories: %d", 
                        request.getFoodName(), request.getServingUnit(), request.getCaloriesPerUnit()));

        return new FoodResponse(
                savedFood.getFoodId(),
                savedFood.getFoodName(),
                savedFood.getServingUnit(),
                savedFood.getCaloriesPerUnit()
        );
    }

    @Transactional
    public FoodResponse updateFood(String adminEmail, Integer foodId, CustomFoodRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminEmail));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new IllegalArgumentException("Food not found: " + foodId));

        String oldData = String.format("Name: %s, Unit: %s, Calories: %d",
                food.getFoodName(), food.getServingUnit(), food.getCaloriesPerUnit());

        food.setFoodName(request.getFoodName().trim());
        food.setServingUnit(request.getServingUnit().trim());
        food.setCaloriesPerUnit(request.getCaloriesPerUnit());

        Food savedFood = foodRepository.save(food);

        auditLogService.logUpdate(admin, "Food",
                String.valueOf(foodId),
                oldData,
                String.format("Name: %s, Unit: %s, Calories: %d",
                        request.getFoodName(), request.getServingUnit(), request.getCaloriesPerUnit()));

        return new FoodResponse(
                savedFood.getFoodId(),
                savedFood.getFoodName(),
                savedFood.getServingUnit(),
                savedFood.getCaloriesPerUnit()
        );
    }

    @Transactional
    public void deleteFood(String adminEmail, Integer foodId) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminEmail));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new IllegalArgumentException("Food not found: " + foodId));

        String deletedData = String.format("Name: %s, Unit: %s, Calories: %d",
                food.getFoodName(), food.getServingUnit(), food.getCaloriesPerUnit());

        foodRepository.delete(food);

        auditLogService.logDelete(admin, "Food",
                String.valueOf(foodId),
                deletedData);
    }

    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll().stream()
                .map(e -> new ExerciseResponse(
                        e.getExerciseId(),
                        e.getExerciseName(),
                        e.getCaloriesBurnedPerHour()))
                .collect(Collectors.toList());
    }

    public List<ExerciseResponse> searchExercises(String query) {
        return exerciseRepository.searchByName(query.trim()).stream()
                .map(e -> new ExerciseResponse(
                        e.getExerciseId(),
                        e.getExerciseName(),
                        e.getCaloriesBurnedPerHour()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ExerciseResponse createExercise(String adminEmail, CustomExerciseRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminEmail));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        Exercise exercise = new Exercise();
        exercise.setExerciseName(request.getExerciseName().trim());
        exercise.setCaloriesBurnedPerHour(request.getCaloriesBurnedPerHour());
        exercise.setCreatedByAdmin(admin);
        exercise.setCreatedByUser(null);
        exercise.setIsPublic(true);

        Exercise savedExercise = exerciseRepository.save(exercise);

        auditLogService.logCreate(admin, "Exercise",
                String.valueOf(savedExercise.getExerciseId()),
                String.format("Name: %s, Calories/Hour: %d",
                        request.getExerciseName(), request.getCaloriesBurnedPerHour()));

        return new ExerciseResponse(
                savedExercise.getExerciseId(),
                savedExercise.getExerciseName(),
                savedExercise.getCaloriesBurnedPerHour()
        );
    }

    @Transactional
    public ExerciseResponse updateExercise(String adminEmail, Integer exerciseId, CustomExerciseRequest request) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminEmail));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + exerciseId));

        String oldData = String.format("Name: %s, Calories/Hour: %d",
                exercise.getExerciseName(), exercise.getCaloriesBurnedPerHour());

        exercise.setExerciseName(request.getExerciseName().trim());
        exercise.setCaloriesBurnedPerHour(request.getCaloriesBurnedPerHour());

        Exercise savedExercise = exerciseRepository.save(exercise);

        auditLogService.logUpdate(admin, "Exercise",
                String.valueOf(exerciseId),
                oldData,
                String.format("Name: %s, Calories/Hour: %d",
                        request.getExerciseName(), request.getCaloriesBurnedPerHour()));

        return new ExerciseResponse(
                savedExercise.getExerciseId(),
                savedExercise.getExerciseName(),
                savedExercise.getCaloriesBurnedPerHour()
        );
    }

    @Transactional
    public void deleteExercise(String adminEmail, Integer exerciseId) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found: " + adminEmail));
        
        if (admin.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not an admin");
        }

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found: " + exerciseId));

        String deletedData = String.format("Name: %s, Calories/Hour: %d",
                exercise.getExerciseName(), exercise.getCaloriesBurnedPerHour());

        exerciseRepository.delete(exercise);

        auditLogService.logDelete(admin, "Exercise",
                String.valueOf(exerciseId),
                deletedData);
    }

    // ========== USER MANAGEMENT METHODS ==========

    public List<UserSummaryResponse> getAllUsers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;

        if (query != null && !query.trim().isEmpty()) {
            // For simplicity, search by email or name - would need custom query
            userPage = userRepository.findAll(pageable); // Placeholder
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return userPage.getContent().stream()
                .map(this::convertToUserSummaryResponse)
                .collect(Collectors.toList());
    }

    private UserSummaryResponse convertToUserSummaryResponse(User user) {
        return new UserSummaryResponse(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                null, // createdAt - not available in User entity
                null, // lastLogin - not available in User entity
                true  // active - assuming all users are active for now
        );
    }

    public UserDetailResponse getUserDetails(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // Get user profile
        var profile = userProfileRepository.findByUserUserId(userId).orElse(null);

        // Get statistics
        long totalFoodLogs = userFoodLogRepository.countByUserUserId(userId);
        long totalExerciseLogs = userExerciseLogRepository.countByUserUserId(userId);
        long totalAchievements = userAchievementRepository.countByUserUserId(userId);
        long customFoodsCount = foodRepository.countByCreatedByUserUserId(userId);
        long customExercisesCount = exerciseRepository.countByCreatedByUserUserId(userId);

        return new UserDetailResponse(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                null, // createdAt - not available in User entity
                null, // lastLogin - not available in User entity
                true, // active - assuming all users are active for now
                profile != null ? profile.getGender() : null,
                profile != null ? profile.getGoalWeightKg() : null,
                profile != null ? profile.getDailyCalorieGoal() : null,
                totalFoodLogs,
                totalExerciseLogs,
                totalAchievements,
                customFoodsCount,
                customExercisesCount
        );
    }

    // ========== AUDIT LOG METHODS ==========

    public List<AuditLogResponse> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditPage = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);

        return auditPage.getContent().stream()
                .map(this::convertToAuditLogResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getRecentAuditLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<AuditLog> auditPage = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);

        return auditPage.getContent().stream()
                .map(this::convertToAuditLogResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse convertToAuditLogResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getAuditId(),
                auditLog.getUser() != null ? auditLog.getUser().getEmail() : null,
                auditLog.getAction(),
                auditLog.getEntityName(), // entityName instead of entityType
                auditLog.getEntityId(),
                null, // oldData - not available in current entity
                auditLog.getDetails(), // newData - using details field
                auditLog.getCreatedAt(), // createdAt instead of timestamp
                null // ipAddress - not available in current entity
        );
    }
}

