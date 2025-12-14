package vn.vku.udn.hienpc.bmichatbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatResponse;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatbotRule;
import vn.vku.udn.hienpc.bmichatbot.ml.IntentClassifierService;
import vn.vku.udn.hienpc.bmichatbot.ml.IntentType;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatbotRuleRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ChatbotServiceTest {

    @Mock
    private ChatbotRuleRepository chatbotRuleRepository;

    @Mock
    private GeminiApiService geminiApiService;

    @Mock
    private IntentClassifierService intentClassifierService;

    @InjectMocks
    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleMessage_shouldReturnRuleResponseWhenKeywordMatches() {
        ChatbotRule rule = new ChatbotRule();
        rule.setIntent("UPDATE_GOAL"); // Use valid intent string
        rule.setKeywords("tăng cân,weight");
        rule.setResponseTemplate("Đây là câu trả lời rule.");
        rule.setPriority(10);

        // Mock intent classifier with LOW confidence (< 0.6) so rule matching happens first
        // If confidence > 0.6, intent-based response will be used instead of rule
        IntentClassifierService.IntentResult intentResult = new IntentClassifierService.IntentResult(
                IntentType.UPDATE_GOAL, 0.5 // Low confidence, will skip intent-based and use rule
        );
        when(intentClassifierService.classifyWithConfidence("Mình muốn tăng cân nhanh"))
                .thenReturn(intentResult);

        when(chatbotRuleRepository.findAll()).thenReturn(List.of(rule));

        ChatResponse response = chatbotService.handleMessage("Mình muốn tăng cân nhanh", "test-trace");

        assertEquals("Đây là câu trả lời rule.", response.getReply());
    }

    @Test
    void handleMessage_shouldFallbackToGeminiWhenNoRuleMatches() {
        // Mock intent classifier with low confidence - use UNKNOWN or GREETING
        IntentClassifierService.IntentResult intentResult = new IntentClassifierService.IntentResult(
                IntentType.UNKNOWN, 0.5 // Low confidence, will fallback to Gemini
        );
        when(intentClassifierService.classifyWithConfidence("Hello"))
                .thenReturn(intentResult);

        when(chatbotRuleRepository.findAll()).thenReturn(List.of());
        when(geminiApiService.ask("Hello")).thenReturn("fallback");

        ChatResponse response = chatbotService.handleMessage("Hello", "test-trace");

        assertEquals("fallback", response.getReply());
    }
}


