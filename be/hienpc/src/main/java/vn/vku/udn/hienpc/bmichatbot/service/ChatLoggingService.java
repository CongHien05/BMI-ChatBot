package vn.vku.udn.hienpc.bmichatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vku.udn.hienpc.bmichatbot.entity.ChatRequestLog;
import vn.vku.udn.hienpc.bmichatbot.repository.ChatRequestLogRepository;

import java.time.LocalDateTime;

@Service
public class ChatLoggingService {

    private static final Logger log = LoggerFactory.getLogger(ChatLoggingService.class);

    private final ChatRequestLogRepository chatRequestLogRepository;

    public ChatLoggingService(ChatRequestLogRepository chatRequestLogRepository) {
        this.chatRequestLogRepository = chatRequestLogRepository;
    }

    @Transactional
    public void logCompletedRequest(String traceId,
                                    String userEmail,
                                    String source,
                                    long durationMs) {
        ChatRequestLog entity = new ChatRequestLog();
        entity.setTraceId(traceId);
        entity.setUserEmail(userEmail);
        entity.setEvent("CHAT_COMPLETED");
        entity.setSource(source);
        entity.setDurationMs(durationMs);
        entity.setCreatedAt(LocalDateTime.now());

        chatRequestLogRepository.save(entity);
    }

    @Transactional
    public void logFailedRequest(String traceId,
                                 String userEmail,
                                 String errorMessage) {
        ChatRequestLog entity = new ChatRequestLog();
        entity.setTraceId(traceId);
        entity.setUserEmail(userEmail);
        entity.setEvent("CHAT_FAILED");
        entity.setErrorMessage(errorMessage);
        entity.setCreatedAt(LocalDateTime.now());

        chatRequestLogRepository.save(entity);

        log.warn("chat_request_failed traceId={} error={}", traceId, errorMessage);
    }
}


