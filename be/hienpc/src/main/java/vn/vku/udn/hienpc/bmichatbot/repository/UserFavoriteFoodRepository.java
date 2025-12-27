package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserFavoriteFood;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteFoodRepository extends JpaRepository<UserFavoriteFood, Integer> {
    
    List<UserFavoriteFood> findByUserOrderByAddedAtDesc(User user);
    
    Optional<UserFavoriteFood> findByUserAndFood_FoodId(User user, Integer foodId);
    
    boolean existsByUserAndFood_FoodId(User user, Integer foodId);
    
    void deleteByUserAndFood_FoodId(User user, Integer foodId);
    
    @Query("SELECT f.food FROM UserFavoriteFood f WHERE f.user = :user ORDER BY f.addedAt DESC")
    List<vn.vku.udn.hienpc.bmichatbot.entity.Food> findFoodsByUser(@Param("user") User user);
}

