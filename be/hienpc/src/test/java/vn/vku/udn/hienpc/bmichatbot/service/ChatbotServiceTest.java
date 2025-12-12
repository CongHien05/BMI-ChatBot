package vn.vku.udn.hienpc.bmichatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatbotRule;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatbotRuleRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ChatbotServiceTest {

    @Mock
    private ChatbotRuleRepository chatbotRuleRepository;

    @Mock
    private GeminiApiService geminiApiService;

    @InjectMocks
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleMessage_shouldReturnRuleResponseWhenKeywordMatches() {
        ChatbotRule rule = new ChatbotRule();
        rule.setIntent("FAQ");
        rule.setKeywords("tăng cân,weight");
        rule.setResponseTemplate("Đây là câu trả lời rule.");
        rule.setPriority(10);

        when(chatbotRuleRepository.findAll()).thenReturn(List.of(rule));

        ChatResponse response = chatbotService.handleMessage("Mình muốn tăng cân nhanh", "test-trace");

        assertEquals("Đây là câu trả lời rule.", response.getReply());
    }

    @Test
    void handleMessage_shouldFallbackToGeminiWhenNoRuleMatches() {
        when(chatbotRuleRepository.findAll()).thenReturn(List.of());
        when(geminiApiService.ask("Hello")).thenReturn("fallback");

        ChatResponse response = chatbotService.handleMessage("Hello", "test-trace");

        assertEquals("fallback", response.getReply());
    }
}


