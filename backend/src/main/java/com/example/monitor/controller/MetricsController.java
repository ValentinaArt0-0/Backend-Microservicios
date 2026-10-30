package com.example.monitor.controller;

import com.example.monitor.model.LogEntry;
import com.example.monitor.model.LogStatus;
import com.example.monitor.model.MetricsSummary;
import com.example.monitor.model.PageResult;
import com.example.monitor.service.LoadSimulationService;
import com.example.monitor.service.MetricsService;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;
    private final LoadSimulationService loadSimulationService;

    public MetricsController(MetricsService metricsService, LoadSimulationService loadSimulationService) {
        this.metricsService = metricsService;
        this.loadSimulationService = loadSimulationService;
    }

    @GetMapping("/summary")
    public MetricsSummary summary() {
        return metricsService.summary();
    }

    @GetMapping("/logs")
    public PageResult<LogEntry> logs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) LogStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return metricsService.logs(service, status, from, to, page, size);
    }

    @PostMapping("/simulate-load")
    public Map<String, Object> simulateLoad() {
        return loadSimulationService.simulate();
    }
}
