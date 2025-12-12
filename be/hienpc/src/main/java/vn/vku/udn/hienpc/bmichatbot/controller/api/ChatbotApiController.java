package vn.vku.udn.hienpc.bmichatbot.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.vku.udn.hienpc.bmichatbot.dto.request.ChatRequest;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatConversationSummaryResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatHistoryItemResponse;
import vn.vku.udn.hienpc.bmichatbot.dto.response.ChatResponse;
import vn.vku.udn.hienpc.bmichatbot.service.ChatConversationService;
import vn.vku.udn.hienpc.bmichatbot.service.ChatLoggingService;
import vn.vku.udn.hienpc.bmichatbot.service.ChatbotService;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "*")
public class ChatbotApiController {

    private static final Logger log = LoggerFactory.getLogger(ChatbotApiController.class);

    private final ChatbotService chatbotService;
    private final ChatConversationService chatConversationService;
    private final ChatLoggingService chatLoggingService;

    public ChatbotApiController(ChatbotService chatbotService,
                                ChatConversationService chatConversationService,
                                ChatLoggingService chatLoggingService) {
        this.chatbotService = chatbotService;
        this.chatConversationService = chatConversationService;
        this.chatLoggingService = chatLoggingService;
    }

    @PostMapping
    @Operation(summary = "Chat with BMI chatbot", description = "Smart routing between rule-based replies and Gemini fallback")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        long start = System.currentTimeMillis();
        String traceId = java.util.UUID.randomUUID().toString();

        log.info("chat_request_received traceId={} messagePreview=\"{}\"",
                traceId,
                truncate(request.getMessage(), 120));

        ChatResponse response = null;
        String userEmail = resolveCurrentUserEmail();
        long duration;

        try {
            response = chatbotService.handleMessage(request.getMessage(), traceId);
            duration = System.currentTimeMillis() - start;
            response.setDurationMs(duration);

            // Lưu hội thoại (conversation + messages)
            // Nếu request.conversationId != null và tồn tại -> append vào đó, ngược lại tạo mới
            Long conversationId = chatConversationService.logExchange(
                    userEmail,
                    "ANDROID",
                    request.getConversationId(),
                    request.getMessage(),
                    response
            );
            response.setConversationId(conversationId);

            // Ghi log DB cho request thành công
            chatLoggingService.logCompletedRequest(
                    traceId,
                    userEmail,
                    response.getSource(),
                    duration
            );

            log.info("chat_response_sent traceId={} source={} durationMs={}",
                    traceId,
                    response.getSource(),
                    duration);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            duration = System.currentTimeMillis() - start;
            chatLoggingService.logFailedRequest(traceId, userEmail, ex.getMessage());
            throw ex;
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get recent chat history for current user")
    public ResponseEntity<?> getHistory() {
        String userEmail = resolveCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.ok(java.util.List.of());
        }

        java.util.List<ChatHistoryItemResponse> history =
                chatConversationService.getRecentMessagesForUser(userEmail, 20);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get list of all conversations for current user")
    public ResponseEntity<?> getConversations() {
        String userEmail = resolveCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.ok(java.util.List.of());
        }

        java.util.List<ChatConversationSummaryResponse> conversations =
                chatConversationService.getConversationsForUser(userEmail);

        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{conversationId}")
    @Operation(summary = "Get all messages of a specific conversation")
    public ResponseEntity<?> getConversationMessages(@PathVariable Long conversationId) {
        String userEmail = resolveCurrentUserEmail();
        if (userEmail == null) {
            return ResponseEntity.ok(java.util.List.of());
        }

        java.util.List<ChatHistoryItemResponse> messages =
                chatConversationService.getMessagesByConversation(conversationId, userEmail);

        return ResponseEntity.ok(messages);
    }

    private String resolveCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }

    private String truncate(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...";
    }
}
