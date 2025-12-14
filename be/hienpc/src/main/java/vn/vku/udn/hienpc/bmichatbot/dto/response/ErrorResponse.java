package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String code;        // Error code for client handling
    private String message;     // User-friendly message (Vietnamese)
    private List<String> details; // Additional details (e.g., validation errors)
    private String path;

    // Constructor for simple errors without details
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String code, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.details = null;
        this.path = path;
    }
}


