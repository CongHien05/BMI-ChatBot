package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.FoodResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.*;
import vn.vku.udn.hienpc.bmichatbot.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final UserFavoriteFoodRepository userFavoriteFoodRepository;
    private final UserFavoriteExerciseRepository userFavoriteExerciseRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final ExerciseRepository exerciseRepository;

    public FavoriteService(
            UserFavoriteFoodRepository userFavoriteFoodRepository,
            UserFavoriteExerciseRepository userFavoriteExerciseRepository,
            UserRepository userRepository,
            FoodRepository foodRepository,
            ExerciseRepository exerciseRepository) {
        this.userFavoriteFoodRepository = userFavoriteFoodRepository;
        this.userFavoriteExerciseRepository = userFavoriteExerciseRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.exerciseRepository = exerciseRepository;
    }

    // ========== FOOD FAVORITES ==========

    public List<FoodResponse> getFavoriteFoods(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        List<Food> foods = userFavoriteFoodRepository.findFoodsByUser(user);
        return foods.stream()
                .map(f -> new FoodResponse(
                        f.getFoodId(),
                        f.getFoodName(),
                        f.getServingUnit(),
                        f.getCaloriesPerUnit()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavoriteFood(String userEmail, Integer foodId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new RuntimeException("Food not found: " + foodId));
        
        if (userFavoriteFoodRepository.existsByUserAndFood_FoodId(user, foodId)) {
            throw new RuntimeException("Food already in favorites");
        }
        
        UserFavoriteFood favorite = new UserFavoriteFood();
        favorite.setUser(user);
        favorite.setFood(food);
        userFavoriteFoodRepository.save(favorite);
    }

    @Transactional
    public void removeFavoriteFood(String userEmail, Integer foodId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        userFavoriteFoodRepository.deleteByUserAndFood_FoodId(user, foodId);
    }

    public boolean isFoodFavorite(String userEmail, Integer foodId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        return userFavoriteFoodRepository.existsByUserAndFood_FoodId(user, foodId);
    }

    // ========== EXERCISE FAVORITES ==========

    public List<ExerciseResponse> getFavoriteExercises(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        List<Exercise> exercises = userFavoriteExerciseRepository.findExercisesByUser(user);
        return exercises.stream()
                .map(e -> new ExerciseResponse(
                        e.getExerciseId(),
                        e.getExerciseName(),
                        e.getCaloriesBurnedPerHour()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavoriteExercise(String userEmail, Integer exerciseId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));
        
        if (userFavoriteExerciseRepository.existsByUserAndExercise_ExerciseId(user, exerciseId)) {
            throw new RuntimeException("Exercise already in favorites");
        }
        
        UserFavoriteExercise favorite = new UserFavoriteExercise();
        favorite.setUser(user);
        favorite.setExercise(exercise);
        userFavoriteExerciseRepository.save(favorite);
    }

    @Transactional
    public void removeFavoriteExercise(String userEmail, Integer exerciseId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        userFavoriteExerciseRepository.deleteByUserAndExercise_ExerciseId(user, exerciseId);
    }

    public boolean isExerciseFavorite(String userEmail, Integer exerciseId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
        
        return userFavoriteExerciseRepository.existsByUserAndExercise_ExerciseId(user, exerciseId);
    }
}

