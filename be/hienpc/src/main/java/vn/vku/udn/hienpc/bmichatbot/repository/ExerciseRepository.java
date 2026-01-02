package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.vku.udn.hienpc.bmichatbot.entity.Exercise;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    
    /**
     * Search exercises by name (case-insensitive, partial match)
     * Results are ordered by: exact match > starts with > contains
     * @param query Search query
     * @return List of exercises matching the query, sorted by relevance
     */
    @Query("SELECT e FROM Exercise e WHERE LOWER(e.exerciseName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY " +
           "CASE WHEN LOWER(e.exerciseName) = LOWER(:query) THEN 1 " +
           "WHEN LOWER(e.exerciseName) LIKE LOWER(CONCAT(:query, '%')) THEN 2 " +
           "ELSE 3 END, " +
           "e.exerciseName")
    List<Exercise> searchByName(@Param("query") String query);
    
    /**
     * Get custom exercises created by a specific user
     */
    @Query("SELECT e FROM Exercise e WHERE e.createdByUser.userId = :userId AND e.isPublic = false")
    List<Exercise> findCustomExercisesByUser(@Param("userId") Integer userId);
    
    /**
     * Check if exercise name exists (case-insensitive)
     */
    @Query("SELECT COUNT(e) > 0 FROM Exercise e WHERE LOWER(TRIM(e.exerciseName)) = LOWER(TRIM(:exerciseName))")
    boolean existsByExerciseNameIgnoreCase(@Param("exerciseName") String exerciseName);

    /**
     * Count custom exercises created by users (not admin)
     */
    long countByCreatedByUserIsNotNull();

    /**
     * Count exercises created by a specific user
     */
    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.createdByUser.userId = :userId")
    long countByCreatedByUserUserId(@Param("userId") Integer userId);
}


