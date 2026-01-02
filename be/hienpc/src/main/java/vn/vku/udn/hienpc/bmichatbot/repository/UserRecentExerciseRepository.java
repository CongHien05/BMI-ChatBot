package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserRecentExercise;

import java.util.List;
import java.util.Optional;

public interface UserRecentExerciseRepository extends JpaRepository<UserRecentExercise, Integer> {
    
    List<UserRecentExercise> findByUserOrderByLastUsedAtDesc(User user);
    
    Optional<UserRecentExercise> findByUserAndExercise_ExerciseId(User user, Integer exerciseId);
    
    @Query("SELECT e.exercise FROM UserRecentExercise e WHERE e.user = :user ORDER BY e.lastUsedAt DESC")
    List<vn.vku.udn.hienpc.bmichatbot.entity.Exercise> findExercisesByUserOrderByLastUsedAtDesc(
        @Param("user") User user,
        Pageable pageable
    );
}

