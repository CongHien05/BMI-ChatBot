package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatbotRule;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatbotRuleRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ChatbotService {

    private final ChatbotRuleRepository chatbotRuleRepository;
    private final GeminiApiService geminiApiService;

    public ChatbotService(ChatbotRuleRepository chatbotRuleRepository,
                          GeminiApiService geminiApiService) {
        this.chatbotRuleRepository = chatbotRuleRepository;
        this.geminiApiService = geminiApiService;
    }

    public ChatResponse handleMessage(String userMessage, String traceId) {
        if (!StringUtils.hasText(userMessage)) {
            return ChatResponse.builder()
                    .reply("Xin lỗi, mình không nhận được nội dung tin nhắn của bạn.")
                    .source("RULE")
                    .traceId(traceId)
                    .durationMs(0L)
                    .build();
        }

        String normalized = userMessage.toLowerCase(Locale.ROOT);

        // Bước 1: thử tìm rule phù hợp theo keywords (simple contains)
        // Sao chép sang danh sách mutable để tránh lỗi khi repository trả về danh sách bất biến
        List<ChatbotRule> allRules = new java.util.ArrayList<>(chatbotRuleRepository.findAll());
        allRules.sort(Comparator.comparing(ChatbotRule::getPriority).reversed());

        for (ChatbotRule rule : allRules) {
            if (matchRuleByKeywords(rule, normalized)) {
                // Tạm thời chỉ trả về responseTemplate,
                // sau này có thể xử lý intent đặc biệt (ADD_WEIGHT, v.v.)
                return ChatResponse.builder()
                        .reply(rule.getResponseTemplate())
                        .source("RULE")
                        .traceId(traceId)
                        .durationMs(0L)
                        .build();
            }
        }

        // Bước 2: nếu không có rule nào match, gọi Gemini fallback
        String fallback = geminiApiService.ask(userMessage);
        return ChatResponse.builder()
                .reply(fallback)
                .source("GEMINI")
                .traceId(traceId)
                .durationMs(0L)
                .build();
    }

    private boolean matchRuleByKeywords(ChatbotRule rule, String normalizedMessage) {
        if (!StringUtils.hasText(rule.getKeywords())) {
            return false;
        }
        String[] tokens = rule.getKeywords().split(",");
        for (String rawKeyword : tokens) {
            String keyword = rawKeyword.trim().toLowerCase(Locale.ROOT);
            if (!keyword.isEmpty() && normalizedMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
