package vn.vku.udn.hienpc.bmichatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_request_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "trace_id", nullable = false, length = 100)
    private String traceId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "event", nullable = false, length = 50)
    private String event; // ví dụ: CHAT_COMPLETED, CHAT_FAILED

    @Column(name = "source", length = 20)
    private String source; // RULE hoặc GEMINI

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}


