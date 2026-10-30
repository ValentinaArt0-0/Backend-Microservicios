package com.example.monitor.model;

import java.util.List;
import java.util.Map;

public record MetricsSummary(
        long totalCalls,
        long successCalls,
        long errorCalls,
        double errorRate,
        double averageDuration,
        Map<String, Long> callsByService,
        List<ServiceMetrics> services) {
}
