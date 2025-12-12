package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.vku.udn.hienpc.bmichatbot.entity.Food;

@Repository
public interface FoodRepository extends JpaRepository<Food, Integer> {
}


