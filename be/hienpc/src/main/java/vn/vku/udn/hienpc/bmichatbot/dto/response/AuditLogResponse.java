package vn.vku.udn.hienpc.bmichatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long auditId;
    private String adminEmail;
    private String action;
    private String entityType;
    private String entityId;
    private String oldData;
    private String newData;
    private LocalDateTime timestamp;
    private String ipAddress;
}
