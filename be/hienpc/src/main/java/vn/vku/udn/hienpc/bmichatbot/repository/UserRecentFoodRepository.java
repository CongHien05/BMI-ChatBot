package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.entity.UserRecentFood;

import java.util.List;
import java.util.Optional;

public interface UserRecentFoodRepository extends JpaRepository<UserRecentFood, Integer> {
    
    List<UserRecentFood> findByUserOrderByLastUsedAtDesc(User user);
    
    Optional<UserRecentFood> findByUserAndFood_FoodId(User user, Integer foodId);
    
    @Query("SELECT f.food FROM UserRecentFood f WHERE f.user = :user ORDER BY f.lastUsedAt DESC")
    List<vn.vku.udn.hienpc.bmichatbot.entity.Food> findFoodsByUserOrderByLastUsedAtDesc(
        @Param("user") User user,
        Pageable pageable
    );
}

