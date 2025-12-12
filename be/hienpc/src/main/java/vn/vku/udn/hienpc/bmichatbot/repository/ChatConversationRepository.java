package vn.vku.udn.hienpc.bmichatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatConversation;
import vn.vku.udn.hienpc.bmichatbot.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

    // Lấy tất cả conversations của một user, sort theo startedAt DESC (mới nhất trước)
    List<ChatConversation> findByUserOrderByStartedAtDesc(User user);

    // Tìm conversation theo ID và user (để check ownership)
    Optional<ChatConversation> findByConversationIdAndUser(Long conversationId, User user);
}


