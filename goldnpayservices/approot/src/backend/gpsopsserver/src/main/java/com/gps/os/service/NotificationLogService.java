package com.gps.os.service;

import com.gps.os.entity.Channel;
import com.gps.os.entity.DeliveryStatus;
import com.gps.os.entity.NotificationLog;
import com.gps.os.repository.NotificationLogRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/** Every send is recorded here; request_id is unique so a repeated request is detected. */
@Service
@RequiredArgsConstructor
public class NotificationLogService {
    private final NotificationLogRepository repo;

    public record Created(NotificationLog log, boolean isNew) {}

    public Optional<NotificationLog> find(String requestId) {
        return repo.findByRequestId(requestId);
    }

    public Optional<NotificationLog> findById(Long id) {
        return repo.findById(id);
    }

    /** Creates a QUEUED row, or returns the existing one when the requestId was already accepted. */
    public Created createIfAbsent(String requestId, Channel channel, String category, Long userId, String recipient) {
        Optional<NotificationLog> existing = repo.findByRequestId(requestId);
        if (existing.isPresent()) return new Created(existing.get(), false);
        NotificationLog n = new NotificationLog();
        n.setRequestId(requestId);
        n.setChannel(channel);
        n.setCategory(category);
        n.setUserId(userId);
        n.setRecipient(recipient == null ? null : cut(recipient, 500));
        n.setStatus(DeliveryStatus.QUEUED);
        try {
            return new Created(repo.saveAndFlush(n), true);
        } catch (DataIntegrityViolationException race) {
            return new Created(repo.findByRequestId(requestId).orElseThrow(() -> race), false);
        }
    }

    public void markAttempt(Long id, int attempts) {
        repo.findById(id).ifPresent(n -> {
            n.setAttempts(attempts);
            repo.save(n);
        });
    }

    public void markSent(Long id, int attempts) {
        repo.findById(id).ifPresent(n -> {
            n.setAttempts(attempts);
            n.setStatus(DeliveryStatus.SENT);
            n.setError(null);
            n.setSentAt(Instant.now());
            repo.save(n);
        });
    }

    public void markFailed(Long id, int attempts, String error) {
        repo.findById(id).ifPresent(n -> {
            n.setAttempts(attempts);
            n.setStatus(DeliveryStatus.FAILED);
            n.setError(error == null ? null : cut(error, 1000));
            repo.save(n);
        });
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
