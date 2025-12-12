package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import vn.vku.udn.hienpc.bmichatbot.dto.response.RecommendationResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.RecommendationResponse.RecommendedItem;
import vn.vku.udn.hienpc.bmichatbot.entity.*;
import vn.vku.udn.hienpc.bmichatbot.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final BodyMeasurementRepository measurementRepository;
    private final UserFoodLogRepository foodLogRepository;
    private final UserExerciseLogRepository exerciseLogRepository;
    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;

    public RecommendationService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            BodyMeasurementRepository measurementRepository,
            UserFoodLogRepository foodLogRepository,
            UserExerciseLogRepository exerciseLogRepository,
            FoodRepository foodRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.measurementRepository = measurementRepository;
        this.foodLogRepository = foodLogRepository;
        this.exerciseLogRepository = exerciseLogRepository;
        this.foodRepository = foodRepository;
        this.exerciseRepository = exerciseRepository;
    }

    /**
     * Recommend foods using Collaborative Filtering
     */
    public RecommendationResponse recommendFoods(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        // 1. Find similar users
        List<User> similarUsers = findSimilarUsers(user, 20);
        
        if (similarUsers.isEmpty()) {
            return getDefaultFoodRecommendations(limit);
        }

        // 2. Get popular foods from similar users (last 30 days)
        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        Map<Integer, Integer> foodPopularity = new HashMap<>();
        
        for (User similarUser : similarUsers) {
            List<UserFoodLog> logs = foodLogRepository
                    .findByUserUserIdAndDateEatenAfter(
                            similarUser.getUserId(), 
                            cutoffDate.atStartOfDay()
                    );
            
            for (UserFoodLog log : logs) {
                Integer foodId = log.getFood().getFoodId();
                foodPopularity.put(foodId, foodPopularity.getOrDefault(foodId, 0) + 1);
            }
        }

        // 3. Get user's recent foods (to avoid recommending same foods)
        Set<Integer> userRecentFoods = foodLogRepository
                .findByUserUserIdAndDateEatenAfter(user.getUserId(), cutoffDate.atStartOfDay())
                .stream()
                .map(log -> log.getFood().getFoodId())
                .collect(Collectors.toSet());

        // 4. Score and rank foods
        List<RecommendedItem> recommendations = new ArrayList<>();
        
        for (Map.Entry<Integer, Integer> entry : foodPopularity.entrySet()) {
            Integer foodId = entry.getKey();
            Integer popularityCount = entry.getValue();
            
            // Skip if user already ate this recently
            if (userRecentFoods.contains(foodId)) {
                continue;
            }
            
            Food food = foodRepository.findById(foodId).orElse(null);
            if (food == null) continue;
            
            // Calculate score (0-1)
            double score = (double) popularityCount / similarUsers.size();
            
            // Generate reason
            String reason = generateFoodReason(food, popularityCount, user);
            
            recommendations.add(new RecommendedItem(
                    food.getFoodId(),
                    food.getFoodName(),
                    reason,
                    score,
                    popularityCount,
                    food.getServingUnit(),
                    food.getCaloriesPerUnit()
                ));
        }

        // 5. Sort by score and limit
        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        List<RecommendedItem> topRecommendations = recommendations.stream()
                .limit(limit)
                .collect(Collectors.toList());

        // 6. Generate explanation
        String explanation = String.format(
                "Dựa trên %d người dùng tương tự với bạn (cùng mục tiêu và mức độ hoạt động)",
                similarUsers.size()
        );

        return new RecommendationResponse(topRecommendations, explanation, similarUsers.size());
    }

    /**
     * Recommend exercises using Collaborative Filtering
     */
    public RecommendationResponse recommendExercises(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        // Similar logic as foods
        List<User> similarUsers = findSimilarUsers(user, 20);
        
        if (similarUsers.isEmpty()) {
            return getDefaultExerciseRecommendations(limit);
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        Map<Integer, Integer> exercisePopularity = new HashMap<>();
        
        for (User similarUser : similarUsers) {
            List<UserExerciseLog> logs = exerciseLogRepository
                    .findByUserUserIdAndDateExercisedAfter(
                            similarUser.getUserId(), 
                            cutoffDate.atStartOfDay()
                    );
            
            for (UserExerciseLog log : logs) {
                Integer exerciseId = log.getExercise().getExerciseId();
                exercisePopularity.put(exerciseId, exercisePopularity.getOrDefault(exerciseId, 0) + 1);
            }
        }

        Set<Integer> userRecentExercises = exerciseLogRepository
                .findByUserUserIdAndDateExercisedAfter(user.getUserId(), cutoffDate.atStartOfDay())
                .stream()
                .map(log -> log.getExercise().getExerciseId())
                .collect(Collectors.toSet());

        List<RecommendedItem> recommendations = new ArrayList<>();
        
        for (Map.Entry<Integer, Integer> entry : exercisePopularity.entrySet()) {
            Integer exerciseId = entry.getKey();
            Integer popularityCount = entry.getValue();
            
            if (userRecentExercises.contains(exerciseId)) {
                continue;
            }
            
            Exercise exercise = exerciseRepository.findById(exerciseId).orElse(null);
            if (exercise == null) continue;
            
            double score = (double) popularityCount / similarUsers.size();
            String reason = generateExerciseReason(exercise, popularityCount, user);
            
            recommendations.add(new RecommendedItem(
                    exercise.getExerciseId(),
                    exercise.getExerciseName(),
                    reason,
                    score,
                    popularityCount,
                    "Exercise",  // Default type
                    exercise.getCaloriesBurnedPerHour()
                ));
        }

        recommendations.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        List<RecommendedItem> topRecommendations = recommendations.stream()
                .limit(limit)
                .collect(Collectors.toList());

        String explanation = String.format(
                "Dựa trên %d người dùng tương tự với bạn",
                similarUsers.size()
        );

        return new RecommendationResponse(topRecommendations, explanation, similarUsers.size());
    }

    /**
     * Find similar users based on:
     * - Weight range (±5kg)
     * - Same goal (lose/gain/maintain)
     * - Similar activity level
     */
    private List<User> findSimilarUsers(User user, int limit) {
        // Get user's profile
        UserProfile userProfile = userProfileRepository.findByUserUserId(user.getUserId())
                .orElse(null);
        
        if (userProfile == null) {
            return Collections.emptyList();
        }

        // Get user's latest weight
        List<BodyMeasurement> userMeasurements = measurementRepository
                .findByUserUserIdOrderByDateRecordedDesc(user.getUserId());
        
        if (userMeasurements.isEmpty()) {
            return Collections.emptyList();
        }
        
        BigDecimal userWeight = userMeasurements.get(0).getWeightKg();
        String userGoal = userProfile.getGoalType();
        String userActivityLevel = "MODERATE";  // Default if not available

        // Find all users
        List<User> allUsers = userRepository.findAll();
        List<UserSimilarity> similarities = new ArrayList<>();

        for (User otherUser : allUsers) {
            if (otherUser.getUserId().equals(user.getUserId())) {
                continue; // Skip self
            }

            UserProfile otherProfile = userProfileRepository.findByUserUserId(otherUser.getUserId())
                    .orElse(null);
            
            if (otherProfile == null) continue;

            List<BodyMeasurement> otherMeasurements = measurementRepository
                    .findByUserUserIdOrderByDateRecordedDesc(otherUser.getUserId());
            
            if (otherMeasurements.isEmpty()) continue;

            BigDecimal otherWeight = otherMeasurements.get(0).getWeightKg();
            
            // Calculate similarity score
            double similarity = calculateSimilarity(
                    userWeight, userGoal, userActivityLevel,
                    otherWeight, otherProfile.getGoalType(), "MODERATE"
            );

            if (similarity > 0.5) {  // Threshold
                similarities.add(new UserSimilarity(otherUser, similarity));
            }
        }

        // Sort by similarity and limit
        similarities.sort((a, b) -> Double.compare(b.similarity, a.similarity));
        
        return similarities.stream()
                .limit(limit)
                .map(us -> us.user)
                .collect(Collectors.toList());
    }

    /**
     * Calculate similarity between two users (0-1)
     */
    private double calculateSimilarity(
            BigDecimal weight1, String goal1, String activity1,
            BigDecimal weight2, String goal2, String activity2
    ) {
        double similarity = 0.0;
        
        // Weight similarity (±5kg = similar)
        double weightDiff = Math.abs(weight1.doubleValue() - weight2.doubleValue());
        double weightSimilarity = Math.max(0, 1 - (weightDiff / 10.0)); // 10kg = 0 similarity
        similarity += weightSimilarity * 0.4;  // 40% weight
        
        // Goal similarity
        if (goal1 != null && goal1.equals(goal2)) {
            similarity += 0.4;  // 40% weight
        }
        
        // Activity level similarity
        if (activity1 != null && activity1.equals(activity2)) {
            similarity += 0.2;  // 20% weight
        }
        
        return similarity;
    }

    /**
     * Generate reason for food recommendation
     */
    private String generateFoodReason(Food food, Integer popularityCount, User user) {
        if (popularityCount >= 10) {
            return String.format("Rất phổ biến (%d người chọn)", popularityCount);
        } else if (popularityCount >= 5) {
            return String.format("Được ưa chuộng (%d người chọn)", popularityCount);
        } else {
            return String.format("%d người tương tự chọn", popularityCount);
        }
    }

    /**
     * Generate reason for exercise recommendation
     */
    private String generateExerciseReason(Exercise exercise, Integer popularityCount, User user) {
        if (popularityCount >= 10) {
            return String.format("Rất hiệu quả (%d người tập)", popularityCount);
        } else if (popularityCount >= 5) {
            return String.format("Phổ biến (%d người tập)", popularityCount);
        } else {
            return String.format("%d người tương tự tập", popularityCount);
        }
    }

    /**
     * Default recommendations khi không có similar users
     */
    private RecommendationResponse getDefaultFoodRecommendations(int limit) {
        List<Food> popularFoods = foodRepository.findAll().stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        List<RecommendedItem> items = popularFoods.stream()
                .map(food -> new RecommendedItem(
                        food.getFoodId(),
                        food.getFoodName(),
                        "Món ăn phổ biến",
                        0.5,
                        0,
                        food.getServingUnit(),
                        food.getCaloriesPerUnit()
                ))
                .collect(Collectors.toList());
        
        return new RecommendationResponse(
                items,
                "Chưa đủ dữ liệu để gợi ý cá nhân hóa. Đây là các món ăn phổ biến.",
                0
        );
    }

    /**
     * Default recommendations for exercises
     */
    private RecommendationResponse getDefaultExerciseRecommendations(int limit) {
        List<Exercise> popularExercises = exerciseRepository.findAll().stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        List<RecommendedItem> items = popularExercises.stream()
                .map(exercise -> new RecommendedItem(
                        exercise.getExerciseId(),
                        exercise.getExerciseName(),
                        "Bài tập phổ biến",
                        0.5,
                        0,
                        "Exercise",
                        exercise.getCaloriesBurnedPerHour()
                ))
                .collect(Collectors.toList());
        
        return new RecommendationResponse(
                items,
                "Chưa đủ dữ liệu để gợi ý cá nhân hóa. Đây là các bài tập phổ biến.",
                0
        );
    }

    /**
     * Helper class for similarity scoring
     */
    private static class UserSimilarity {
        User user;
        double similarity;
        
        UserSimilarity(User user, double similarity) {
            this.user = user;
            this.similarity = similarity;
        }
    }
}

