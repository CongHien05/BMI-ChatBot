package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.vku.udn.hienpc.bmichatbot.entity.UserFoodLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserFoodLogRepository extends JpaRepository<UserFoodLog, Integer> {

    List<UserFoodLog> findByUserUserIdAndDateEatenBetween(Integer userId, LocalDateTime start, LocalDateTime end);
    
    List<UserFoodLog> findByUserUserIdAndDateEatenAfter(Integer userId, LocalDateTime date);
    
    @Query("SELECT COUNT(DISTINCT ufl.user.userId) FROM UserFoodLog ufl WHERE DATE(ufl.dateEaten) = :date")
    long countDistinctUsersByDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(DISTINCT ufl.user.userId) FROM UserFoodLog ufl WHERE DATE(ufl.dateEaten) BETWEEN :startDate AND :endDate")
    long countDistinctUsersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(ufl) FROM UserFoodLog ufl WHERE DATE(ufl.dateEaten) = :date")
    long countByDate(@Param("date") LocalDate date);

    long countByUserUserId(Integer userId);
}


