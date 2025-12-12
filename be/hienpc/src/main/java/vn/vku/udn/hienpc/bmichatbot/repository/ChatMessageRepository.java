package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatMessage;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatConversation;
import vn.vku.udn.hienpc.bmichatbot.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Lấy 20 message gần nhất của user (theo createdAt DESC)
    List<ChatMessage> findTop20ByConversationUserOrderByCreatedAtDesc(User user);

    // Lấy message mới nhất của một conversation
    Optional<ChatMessage> findTop1ByConversationOrderByCreatedAtDesc(ChatConversation conversation);

    // Lấy tất cả messages của một conversation (sort ASC để hiển thị từ cũ → mới)
    List<ChatMessage> findByConversationOrderByCreatedAtAsc(ChatConversation conversation);

    // Đếm số messages của một conversation
    long countByConversation(ChatConversation conversation);
}


