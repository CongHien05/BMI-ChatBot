package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatConversationSummaryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatHistoryItemResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatConversation;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatMessage;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatConversationRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatMessageRepository;
import vn.vku.udn.hienpc.bmichatbot.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatConversationService {

    private final UserRepository userRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatConversationService(UserRepository userRepository,
                                   ChatConversationRepository chatConversationRepository,
                                   ChatMessageRepository chatMessageRepository) {
        this.userRepository = userRepository;
        this.chatConversationRepository = chatConversationRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Lưu lại một lượt trao đổi (user message + bot reply) vào database.
     * Nếu conversationId != null và tồn tại, sẽ append message vào conversation đó.
     * Nếu conversationId null hoặc không tồn tại, sẽ tạo conversation mới.
     *
     * @return conversationId hiện tại (mới tạo hoặc đã tồn tại)
     */
    @Transactional
    public Long logExchange(String userEmail,
                            String channel,
                            Long conversationId,
                            String userMessage,
                            ChatResponse response) {

        User user = null;
        if (StringUtils.hasText(userEmail)) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        ChatConversation conversation = null;
        if (conversationId != null) {
            conversation = chatConversationRepository.findById(conversationId).orElse(null);
        }

        if (conversation == null) {
            conversation = new ChatConversation();
            conversation.setUser(user);
            conversation.setChannel(channel);
            conversation.setStartedAt(LocalDateTime.now());
            conversation = chatConversationRepository.save(conversation);
        }

        List<ChatMessage> messages = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        ChatMessage userMsg = new ChatMessage();
        userMsg.setConversation(conversation);
        userMsg.setSender("USER");
        userMsg.setContent(userMessage);
        userMsg.setCreatedAt(now);
        messages.add(userMsg);

        ChatMessage botMsg = new ChatMessage();
        botMsg.setConversation(conversation);
        botMsg.setSender("BOT");
        botMsg.setContent(response.getReply());
        botMsg.setCreatedAt(now);
        messages.add(botMsg);

        chatMessageRepository.saveAll(messages);

        return conversation.getConversationId();
    }

    /**
     * Lấy một danh sách các message gần nhất của user (mặc định 20 bản ghi).
     */
    @Transactional(readOnly = true)
    public List<ChatHistoryItemResponse> getRecentMessagesForUser(String userEmail, int limit) {
        if (!StringUtils.hasText(userEmail)) {
            return List.of();
        }

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return List.of();
        }

        List<ChatMessage> messages = chatMessageRepository.findTop20ByConversationUserOrderByCreatedAtDesc(user);

        List<ChatHistoryItemResponse> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            result.add(new ChatHistoryItemResponse(
                    msg.getSender(),
                    msg.getContent(),
                    msg.getCreatedAt()
            ));
        }
        // Đảo lại để message cũ hơn ở trước (từ cũ → mới) nếu cần cho UI
        result.sort(java.util.Comparator.comparing(ChatHistoryItemResponse::getCreatedAt));
        return result;
    }

    /**
     * Lấy danh sách tất cả conversations của user (summary).
     * Sort theo startedAt DESC (mới nhất trước).
     */
    @Transactional(readOnly = true)
    public List<ChatConversationSummaryResponse> getConversationsForUser(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            return List.of();
        }

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return List.of();
        }

        List<ChatConversation> conversations = chatConversationRepository.findByUserOrderByStartedAtDesc(user);
        List<ChatConversationSummaryResponse> result = new ArrayList<>();

        for (ChatConversation conv : conversations) {
            // Lấy message mới nhất
            ChatMessage lastMessage = chatMessageRepository
                    .findTop1ByConversationOrderByCreatedAtDesc(conv)
                    .orElse(null);

            // Đếm tổng số messages
            long totalMessages = chatMessageRepository.countByConversation(conv);

            String lastMessagePreview = "";
            String lastSender = "BOT";
            LocalDateTime lastTime = conv.getStartedAt();

            if (lastMessage != null) {
                lastMessagePreview = truncate(lastMessage.getContent(), 100);
                lastSender = lastMessage.getSender();
                lastTime = lastMessage.getCreatedAt();
            }

            result.add(new ChatConversationSummaryResponse(
                    conv.getConversationId(),
                    lastMessagePreview,
                    lastSender,
                    lastTime,
                    totalMessages
            ));
        }

        return result;
    }

    /**
     * Lấy toàn bộ messages của một conversation cụ thể.
     * Chỉ trả về nếu conversation thuộc về user đó (security check).
     */
    @Transactional(readOnly = true)
    public List<ChatHistoryItemResponse> getMessagesByConversation(Long conversationId, String userEmail) {
        if (conversationId == null || !StringUtils.hasText(userEmail)) {
            return List.of();
        }

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return List.of();
        }

        // Check ownership: conversation phải thuộc về user này
        ChatConversation conversation = chatConversationRepository
                .findByConversationIdAndUser(conversationId, user)
                .orElse(null);

        if (conversation == null) {
            return List.of();
        }

        // Lấy tất cả messages, sort ASC (từ cũ → mới)
        List<ChatMessage> messages = chatMessageRepository.findByConversationOrderByCreatedAtAsc(conversation);

        List<ChatHistoryItemResponse> result = new ArrayList<>();
        for (ChatMessage msg : messages) {
            result.add(new ChatHistoryItemResponse(
                    msg.getSender(),
                    msg.getContent(),
                    msg.getCreatedAt()
            ));
        }

        return result;
    }

    private String truncate(String value, int maxLen) {
        if (value == null) return "";
        if (value.length() <= maxLen) return value;
        return value.substring(0, maxLen) + "...";
    }
}


