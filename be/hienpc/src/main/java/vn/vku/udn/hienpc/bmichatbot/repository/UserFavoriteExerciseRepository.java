package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserFavoriteExercise;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteExerciseRepository extends JpaRepository<UserFavoriteExercise, Integer> {
    
    List<UserFavoriteExercise> findByUserOrderByAddedAtDesc(User user);
    
    Optional<UserFavoriteExercise> findByUserAndExercise_ExerciseId(User user, Integer exerciseId);
    
    boolean existsByUserAndExercise_ExerciseId(User user, Integer exerciseId);
    
    void deleteByUserAndExercise_ExerciseId(User user, Integer exerciseId);
    
    @Query("SELECT e.exercise FROM UserFavoriteExercise e WHERE e.user = :user ORDER BY e.addedAt DESC")
    List<vn.vku.udn.hienpc.bmichatbot.entity.Exercise> findExercisesByUser(@Param("user") User user);
}

