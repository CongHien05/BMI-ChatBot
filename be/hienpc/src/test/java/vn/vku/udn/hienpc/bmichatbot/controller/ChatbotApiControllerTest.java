package vn.vku.udn.hienpc.bmichatbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import vn.vku.udn.hienpc.bmichatbot.controller.api.ChatbotApiController;
import vn.vku.udn.hienpc.bmichatbot.dto.request.ChatRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatHistoryItemResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatResponse;
import vn.vku.udn.hienpc.bmichatbot.service.ChatConversationService;
import vn.vku.udn.hienpc.bmichatbot.service.ChatbotService;
import vn.vku.udn.hienpc.bmichatbot.service.JwtService;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ChatbotApiController.class)
class ChatbotApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatbotService chatbotService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private ChatConversationService chatConversationService;

    @Test
    @WithMockUser(username = "test@example.com")
    void chat_shouldReturnReply() throws Exception {
        Mockito.when(chatbotService.handleMessage(Mockito.eq("Hello"), Mockito.anyString()))
                .thenReturn(ChatResponse.builder()
                        .reply("Hi there")
                        .source("RULE")
                        .traceId("test-trace")
                        .durationMs(5L)
                        .build());

        ChatRequest req = new ChatRequest("Hello");

        mockMvc.perform(post("/api/chatbot")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Hi there"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void history_shouldReturnMessages() throws Exception {
        List<ChatHistoryItemResponse> history = List.of(
                new ChatHistoryItemResponse("USER", "Hello", LocalDateTime.now().minusMinutes(1)),
                new ChatHistoryItemResponse("BOT", "Hi there", LocalDateTime.now())
        );

        Mockito.when(chatConversationService.getRecentMessagesForUser("test@example.com", 20))
                .thenReturn(history);

        mockMvc.perform(get("/api/chatbot/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sender").value("USER"))
                .andExpect(jsonPath("$[1].sender").value("BOT"));
    }
}


