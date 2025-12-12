package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatbotRule;

import java.util.List;

@Repository
public interface ChatbotRuleRepository extends JpaRepository<ChatbotRule, Integer> {

    List<ChatbotRule> findByIntentOrderByPriorityDesc(String intent);
}


