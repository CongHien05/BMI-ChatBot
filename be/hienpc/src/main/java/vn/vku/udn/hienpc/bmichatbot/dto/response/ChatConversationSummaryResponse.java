package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationSummaryResponse {
    private Long conversationId;
    private String lastMessagePreview;
    private String lastSender; // USER hoặc BOT
    private LocalDateTime lastTime;
    private Long totalMessages;
}

