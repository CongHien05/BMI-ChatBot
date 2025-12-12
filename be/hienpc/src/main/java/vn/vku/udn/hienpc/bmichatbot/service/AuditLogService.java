package vn.vku.udn.hienpc.bmichatbot.service;

import org.springframework.stereotype.Service;
import vn.vku.udn.hienpc.bmichatbot.entity.AuditLog;
import vn.vku.udn.hienpc.bmichatbot.entity.User;
import vn.vku.udn.hienpc.bmichatbot.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(User user, String action, String entityName, String entityId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    public void logCreate(User user, String entityName, String entityId, String details) {
        logAction(user, "CREATE", entityName, entityId, details);
    }

    public void logUpdate(User user, String entityName, String entityId, String details) {
        logAction(user, "UPDATE", entityName, entityId, details);
    }

    public void logDelete(User user, String entityName, String entityId, String details) {
        logAction(user, "DELETE", entityName, entityId, details);
    }
}

