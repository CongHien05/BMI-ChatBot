package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.Exercise;
import vn.vku.udn.hienpc.bmichatbot.entity.Food;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserRecentExercise;
import vn.vku.udn.hienpc.bmichatbot.entity.UserRecentFood;
import vn.vku.udn.hienpc.bmichatbot.repository.ExerciseRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.FoodRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRecentExerciseRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRecentFoodRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecentService {

    private final UserRecentFoodRepository userRecentFoodRepository;
    private final UserRecentExerciseRepository userRecentExerciseRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;

    public RecentService(UserRecentFoodRepository userRecentFoodRepository,
                        UserRecentExerciseRepository userRecentExerciseRepository,
                        UserRepository userRepository,
                        FoodRepository foodRepository,
                        ExerciseRepository exerciseRepository) {
        this.userRecentFoodRepository = userRecentFoodRepository;
        this.userRecentExerciseRepository = userRecentExerciseRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.exerciseRepository = exerciseRepository;
    }

    /**
     * Update or create recent food entry when user logs a food
     */
    @Transactional
    public void updateRecentFood(String userEmail, Integer foodId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found: " + foodId));

        Optional<UserRecentFood> existing = userRecentFoodRepository.findByUserAndFood_FoodId(user, foodId);
        
        if (existing.isPresent()) {
            // Update last_used_at
            UserRecentFood recent = existing.get();
            recent.setLastUsedAt(java.time.LocalDateTime.now());
            userRecentFoodRepository.save(recent);
        } else {
            // Create new entry
            UserRecentFood recent = new UserRecentFood();
            recent.setUser(user);
            recent.setFood(food);
            recent.setLastUsedAt(java.time.LocalDateTime.now());
            userRecentFoodRepository.save(recent);
            
            // Keep only last 20 items per user
            List<UserRecentFood> allRecent = userRecentFoodRepository.findByUserOrderByLastUsedAtDesc(user);
            if (allRecent.size() > 20) {
                List<UserRecentFood> toDelete = allRecent.subList(20, allRecent.size());
                userRecentFoodRepository.deleteAll(toDelete);
            }
        }
    }

    /**
     * Update or create recent exercise entry when user logs an exercise
     */
    @Transactional
    public void updateRecentExercise(String userEmail, Integer exerciseId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));

        Optional<UserRecentExercise> existing = userRecentExerciseRepository.findByUserAndExercise_ExerciseId(user, exerciseId);
        
        if (existing.isPresent()) {
            // Update last_used_at
            UserRecentExercise recent = existing.get();
            recent.setLastUsedAt(java.time.LocalDateTime.now());
            userRecentExerciseRepository.save(recent);
        } else {
            // Create new entry
            UserRecentExercise recent = new UserRecentExercise();
            recent.setUser(user);
            recent.setExercise(exercise);
            recent.setLastUsedAt(java.time.LocalDateTime.now());
            userRecentExerciseRepository.save(recent);
            
            // Keep only last 20 items per user
            List<UserRecentExercise> allRecent = userRecentExerciseRepository.findByUserOrderByLastUsedAtDesc(user);
            if (allRecent.size() > 20) {
                List<UserRecentExercise> toDelete = allRecent.subList(20, allRecent.size());
                userRecentExerciseRepository.deleteAll(toDelete);
            }
        }
    }

    /**
     * Get recent foods for a user (limit 10)
     */
    public List<FoodResponse> getRecentFoods(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        List<Food> foods = userRecentFoodRepository.findFoodsByUserOrderByLastUsedAtDesc(
            user, 
            PageRequest.of(0, limit)
        );
        
        return foods.stream()
                .map(f -> new FoodResponse(
                        f.getFoodId(),
                        f.getFoodName(),
                        f.getServingUnit(),
                        f.getCaloriesPerUnit()))
                .collect(Collectors.toList());
    }

    /**
     * Get recent exercises for a user (limit 10)
     */
    public List<ExerciseResponse> getRecentExercises(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        List<Exercise> exercises = userRecentExerciseRepository.findExercisesByUserOrderByLastUsedAtDesc(
            user,
            PageRequest.of(0, limit)
        );
        
        return exercises.stream()
                .map(e -> new ExerciseResponse(
                        e.getExerciseId(),
                        e.getExerciseName(),
                        e.getCaloriesBurnedPerHour()))
                .collect(Collectors.toList());
    }
}

