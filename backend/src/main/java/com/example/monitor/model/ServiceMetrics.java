package com.example.monitor.model;

public record ServiceMetrics(
        String service,
        long totalCalls,
        long successCalls,
        long errorCalls,
        double errorRate,
        double successRate,
        double averageDuration) {
}
