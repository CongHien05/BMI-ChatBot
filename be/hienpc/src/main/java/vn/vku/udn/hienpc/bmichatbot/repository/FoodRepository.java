package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.vku.udn.hienpc.bmichatbot.entity.Food;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Integer> {
    
    /**
     * Search foods by name (case-insensitive, partial match)
     * Results are ordered by: exact match > starts with > contains
     * @param query Search query
     * @return List of foods matching the query, sorted by relevance
     */
    @Query("SELECT f FROM Food f WHERE LOWER(f.foodName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY " +
           "CASE WHEN LOWER(f.foodName) = LOWER(:query) THEN 1 " +
           "WHEN LOWER(f.foodName) LIKE LOWER(CONCAT(:query, '%')) THEN 2 " +
           "ELSE 3 END, " +
           "f.foodName")
    List<Food> searchByName(@Param("query") String query);
    
    /**
     * Get custom foods created by a specific user
     */
    @Query("SELECT f FROM Food f WHERE f.createdByUser.userId = :userId AND f.isPublic = false")
    List<Food> findCustomFoodsByUser(@Param("userId") Integer userId);
    
    /**
     * Check if food name exists (case-insensitive)
     */
    @Query("SELECT COUNT(f) > 0 FROM Food f WHERE LOWER(TRIM(f.foodName)) = LOWER(TRIM(:foodName))")
    boolean existsByFoodNameIgnoreCase(@Param("foodName") String foodName);

    /**
     * Count custom foods created by users (not admin)
     */
    long countByCreatedByUserIsNotNull();

    /**
     * Count foods created by a specific user
     */
    @Query("SELECT COUNT(f) FROM Food f WHERE f.createdByUser.userId = :userId")
    long countByCreatedByUserUserId(@Param("userId") Integer userId);
}


