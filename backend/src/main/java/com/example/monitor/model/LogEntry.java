package com.example.monitor.model;

import java.time.LocalDateTime;

public record LogEntry(
        String requestId,
        String service,
        String operation,
        long duration,
        LogStatus status,
        LocalDateTime timestamp,
        String message) {
}
