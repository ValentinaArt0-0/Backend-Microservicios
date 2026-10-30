package com.example.monitor.service;

import com.example.monitor.model.LogEntry;
import com.example.monitor.model.LogStatus;
import com.example.monitor.model.MetricsSummary;
import com.example.monitor.model.PageResult;
import com.example.monitor.model.ServiceMetrics;
import com.example.monitor.repository.LogRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private static final List<String> SERVICES = List.of("InventoryService", "OrderService", "PaymentService");

    private final LogRepository logRepository;

    public MetricsService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public MetricsSummary summary() {
        List<LogEntry> all = logRepository.findAll();
        Map<String, Long> callsByService = new LinkedHashMap<>();
        List<ServiceMetrics> services = new ArrayList<>();
        for (String name : SERVICES) {
            List<LogEntry> own = all.stream().filter(e -> e.service().equals(name)).toList();
            ServiceMetrics metrics = buildMetrics(name, own);
            callsByService.put(name, metrics.totalCalls());
            services.add(metrics);
        }
        ServiceMetrics global = buildMetrics("Global", all);
        return new MetricsSummary(global.totalCalls(), global.successCalls(), global.errorCalls(),
                global.errorRate(), global.averageDuration(), callsByService, services);
    }

    public PageResult<LogEntry> logs(String service, LogStatus status, LocalDateTime from, LocalDateTime to,
                                     int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        List<LogEntry> filtered = logRepository.find(service, status, from, to);
        int fromIndex = Math.min(safePage * safeSize, filtered.size());
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) safeSize);
        return new PageResult<>(filtered.subList(fromIndex, toIndex), safePage, safeSize, filtered.size(), totalPages);
    }

    private ServiceMetrics buildMetrics(String name, List<LogEntry> entries) {
        long total = entries.size();
        long success = entries.stream().filter(e -> e.status() == LogStatus.SUCCESS).count();
        long errors = total - success;
        double average = entries.stream().collect(Collectors.averagingLong(LogEntry::duration));
        double errorRate = total == 0 ? 0 : round(errors * 100.0 / total);
        double successRate = total == 0 ? 0 : round(success * 100.0 / total);
        return new ServiceMetrics(name, total, success, errors, errorRate, successRate, round(average));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
