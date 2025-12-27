package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
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

    public ExerciseLogService(ExerciseRepository exerciseRepository,
                              UserRepository userRepository,
                              UserExerciseLogRepository userExerciseLogRepository,
                              StreakService streakService) {
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.userExerciseLogRepository = userExerciseLogRepository;
        this.streakService = streakService;
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
        if (query == null || query.trim().isEmpty()) {
            return getAllExercises();
        }
        
        return exerciseRepository.searchByName(query.trim()).stream()
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
}


