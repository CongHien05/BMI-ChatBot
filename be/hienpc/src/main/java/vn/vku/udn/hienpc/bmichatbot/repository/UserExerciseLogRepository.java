package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.vku.udn.hienpc.bmichatbot.entity.UserExerciseLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserExerciseLogRepository extends JpaRepository<UserExerciseLog, Integer> {

    List<UserExerciseLog> findByUserUserIdAndDateExercisedBetween(Integer userId, LocalDateTime start, LocalDateTime end);
    
    List<UserExerciseLog> findByUserUserIdAndDateExercisedAfter(Integer userId, LocalDateTime date);
    
    @Query("SELECT COUNT(DISTINCT uel.user.userId) FROM UserExerciseLog uel WHERE DATE(uel.dateExercised) = :date")
    long countDistinctUsersByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(DISTINCT uel.user.userId) FROM UserExerciseLog uel WHERE DATE(uel.dateExercised) BETWEEN :startDate AND :endDate")
    long countDistinctUsersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(uel) FROM UserExerciseLog uel WHERE DATE(uel.dateExercised) = :date")
    long countByDate(@Param("date") LocalDate date);

    long countByUserUserId(Integer userId);
}


