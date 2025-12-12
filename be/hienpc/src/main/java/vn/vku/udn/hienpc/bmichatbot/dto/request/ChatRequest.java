package vn.vku.udn.hienpc.bmichatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "message is required")
    private String message;

    /**
     * Conversation ID để tiếp tục cuộc trò chuyện cũ.
     * Nếu null hoặc không tồn tại, sẽ tạo conversation mới.
     */
    private Long conversationId;
}
