package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.vku.udn.hienpc.bmichatbot.entity.UserFoodLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserFoodLogRepository extends JpaRepository<UserFoodLog, Integer> {

    List<UserFoodLog> findByUserUserIdAndDateEatenBetween(Integer userId, LocalDateTime start, LocalDateTime end);
}


