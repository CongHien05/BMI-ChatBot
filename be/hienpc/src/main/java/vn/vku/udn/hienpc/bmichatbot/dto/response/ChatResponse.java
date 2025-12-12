package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    /**
     * Câu trả lời gửi về cho người dùng.
     */
    private String reply;

    /**
     * Nguồn sinh ra câu trả lời: RULE hoặc GEMINI.
     */
    private String source;

    /**
     * TraceId để truy vết log end-to-end.
     */
    private String traceId;

    /**
     * Thời gian xử lý (ms) tính từ khi nhận request tới khi trả response.
     */
    private long durationMs;

    /**
     * Conversation ID hiện tại.
     * Dùng để FE tiếp tục cuộc trò chuyện ở lần request tiếp theo.
     */
    private Long conversationId;
}
