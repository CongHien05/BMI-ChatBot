package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vku.udn.hienpc.bmichatbot.dto.request.CustomExerciseRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.request.ExerciseLogRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseLogHistoryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ExerciseResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.Exercise;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserExerciseLog;
import vn.vku.udn.hienpc.bmichatbot.repository.ExerciseRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserExerciseLogRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExerciseLogService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final UserExerciseLogRepository userExerciseLogRepository;
    private final StreakService streakService;
    private final RecentService recentService;

    public ExerciseLogService(ExerciseRepository exerciseRepository,
                              UserRepository userRepository,
                              UserExerciseLogRepository userExerciseLogRepository,
                              StreakService streakService,
                              RecentService recentService) {
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.userExerciseLogRepository = userExerciseLogRepository;
        this.streakService = streakService;
        this.recentService = recentService;
    }

    public List<ExerciseResponse> getAllExercises(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        // Get public exercises: exercises without createdByUser (admin created or old exercises)
        // OR exercises with isPublic = true
        List<Exercise> publicExercises = exerciseRepository.findAll().stream()
                .filter(e -> e.getCreatedByUser() == null || 
                            (e.getIsPublic() != null && e.getIsPublic()))
                .collect(Collectors.toList());
        
        List<Exercise> customExercises = exerciseRepository.findCustomExercisesByUser(user.getUserId());
        
        // Combine and convert to response
        List<Exercise> allExercises = new java.util.ArrayList<>(publicExercises);
        allExercises.addAll(customExercises);
        
        return allExercises.stream()
                .map(e -> new ExerciseResponse(
                        e.getExerciseId(),
                        e.getExerciseName(),
                        e.getCaloriesBurnedPerHour()))
                .collect(Collectors.toList());
    }
    
    public List<ExerciseResponse> searchExercises(String userEmail, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllExercises(userEmail);
        }
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        // Search in public exercises: exercises without createdByUser (admin created or old exercises)
        // OR exercises with isPublic = true
        List<Exercise> publicExercises = exerciseRepository.searchByName(query.trim()).stream()
                .filter(e -> e.getCreatedByUser() == null || 
                            (e.getIsPublic() != null && e.getIsPublic()))
                .collect(Collectors.toList());
        
        // Search in custom exercises of this user
        List<Exercise> customExercises = exerciseRepository.findCustomExercisesByUser(user.getUserId()).stream()
                .filter(e -> e.getExerciseName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        
        // Combine and convert to response
        List<Exercise> allExercises = new java.util.ArrayList<>(publicExercises);
        allExercises.addAll(customExercises);
        
        return allExercises.stream()
                .map(e -> new ExerciseResponse(
                        e.getExerciseId(),
                        e.getExerciseName(),
                        e.getCaloriesBurnedPerHour()))
                .collect(Collectors.toList());
    }

    public void logExercise(String userEmail, ExerciseLogRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + request.getExerciseId()));

        UserExerciseLog log = new UserExerciseLog();
        log.setUser(user);
        log.setExercise(exercise);
        log.setDateExercised(LocalDateTime.now());
        log.setDurationMinutes(request.getDurationMinutes());

        userExerciseLogRepository.save(log);
        
        // Update streak after successful log
        streakService.updateStreak(user.getUserId());
        
        // Update recently used exercises
        recentService.updateRecentExercise(userEmail, request.getExerciseId());
    }

    public List<ExerciseLogHistoryResponse> getExerciseLogHistory(String userEmail, LocalDate from, LocalDate to) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        // Default to last 30 days if not specified
        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusDays(29);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<UserExerciseLog> logs = userExerciseLogRepository
                .findByUserUserIdAndDateExercisedBetween(user.getUserId(), startDateTime, endDateTime);

        return logs.stream()
                .map(log -> {
                    Exercise exercise = log.getExercise();
                    Integer caloriesPerHour = (exercise != null && exercise.getCaloriesBurnedPerHour() != null) 
                            ? exercise.getCaloriesBurnedPerHour() : 0;
                    Double duration = (log.getDurationMinutes() != null) ? log.getDurationMinutes().doubleValue() : 0.0;
                    // Convert per hour to per minute
                    double caloriesPerMinute = caloriesPerHour / 60.0;
                    Integer totalCaloriesBurned = (int) Math.round(duration * caloriesPerMinute);

                    return new ExerciseLogHistoryResponse(
                            log.getLogId(),
                            exercise != null ? exercise.getExerciseId() : null,
                            exercise != null ? exercise.getExerciseName() : "Unknown",
                            duration,
                            (int) Math.round(caloriesPerMinute),
                            totalCaloriesBurned,
                            log.getDateExercised()
                    );
                })
                .collect(Collectors.toList());
    }

    public void updateExerciseLog(String userEmail, Integer logId, ExerciseLogRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        UserExerciseLog log = userExerciseLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise log not found with id: " + logId));

        // Verify log belongs to user
        if (!log.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Exercise log does not belong to user");
        }

        // Only allow editing logs from today
        LocalDate logDate = log.getDateExercised().toLocalDate();
        LocalDate today = LocalDate.now();
        if (!logDate.equals(today)) {
            throw new IllegalArgumentException("Chỉ có thể chỉnh sửa log trong ngày hiện tại");
        }

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + request.getExerciseId()));

        log.setExercise(exercise);
        log.setDurationMinutes(request.getDurationMinutes());

        userExerciseLogRepository.save(log);
        
        // Update streak after successful update
        streakService.updateStreak(user.getUserId());
    }

    public void deleteExerciseLog(String userEmail, Integer logId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        UserExerciseLog log = userExerciseLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise log not found with id: " + logId));

        // Verify log belongs to user
        if (!log.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Exercise log does not belong to user");
        }

        // Only allow deleting logs from today
        LocalDate logDate = log.getDateExercised().toLocalDate();
        LocalDate today = LocalDate.now();
        if (!logDate.equals(today)) {
            throw new IllegalArgumentException("Chỉ có thể xóa log trong ngày hiện tại");
        }

        userExerciseLogRepository.delete(log);
        
        // Update streak after successful delete
        streakService.updateStreak(user.getUserId());
    }
    
    // ========== CUSTOM EXERCISE METHODS ==========
    
    @Transactional
    public ExerciseResponse createCustomExercise(String userEmail, CustomExerciseRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        Exercise exercise = new Exercise();
        exercise.setExerciseName(request.getExerciseName().trim());
        exercise.setCaloriesBurnedPerHour(request.getCaloriesBurnedPerHour());
        exercise.setCreatedByUser(user);
        exercise.setCreatedByAdmin(null);
        exercise.setIsPublic(false); // User-created exercises are private
        
        Exercise savedExercise = exerciseRepository.save(exercise);
        
        return new ExerciseResponse(
                savedExercise.getExerciseId(),
                savedExercise.getExerciseName(),
                savedExercise.getCaloriesBurnedPerHour()
        );
    }
    
    public List<ExerciseResponse> getCustomExercises(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        return exerciseRepository.findCustomExercisesByUser(user.getUserId()).stream()
                .map(e -> new ExerciseResponse(
                        e.getExerciseId(),
                        e.getExerciseName(),
                        e.getCaloriesBurnedPerHour()))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ExerciseResponse updateCustomExercise(String userEmail, Integer exerciseId, CustomExerciseRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + exerciseId));
        
        // Verify exercise belongs to user
        if (exercise.getCreatedByUser() == null || !exercise.getCreatedByUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Exercise does not belong to user or is not a custom exercise");
        }
        
        exercise.setExerciseName(request.getExerciseName().trim());
        exercise.setCaloriesBurnedPerHour(request.getCaloriesBurnedPerHour());
        
        Exercise updatedExercise = exerciseRepository.save(exercise);
        
        return new ExerciseResponse(
                updatedExercise.getExerciseId(),
                updatedExercise.getExerciseName(),
                updatedExercise.getCaloriesBurnedPerHour()
        );
    }
    
    @Transactional
    public void deleteCustomExercise(String userEmail, Integer exerciseId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));
        
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new IllegalArgumentException("Exercise not found with id: " + exerciseId));
        
        // Verify exercise belongs to user
        if (exercise.getCreatedByUser() == null || !exercise.getCreatedByUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Exercise does not belong to user or is not a custom exercise");
        }
        
        exerciseRepository.delete(exercise);
    }
    
    public boolean checkExerciseNameExists(String exerciseName) {
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            return false;
        }
        return exerciseRepository.existsByExerciseNameIgnoreCase(exerciseName.trim());
    }
}


