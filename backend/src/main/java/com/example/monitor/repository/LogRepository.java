package com.example.monitor.repository;

import com.example.monitor.model.LogEntry;
import com.example.monitor.model.LogStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

/** In-memory, thread-safe log storage. */
@Repository
public class LogRepository {

    private static final int MAX_ENTRIES = 5000;

    private final CopyOnWriteArrayList<LogEntry> entries = new CopyOnWriteArrayList<>();

    public synchronized void save(LogEntry entry) {
        entries.add(entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }
    }

    public List<LogEntry> findAll() {
        return new ArrayList<>(entries);
    }

    /** Returns entries matching the filters, newest first. Null filters are ignored. */
    public List<LogEntry> find(String service, LogStatus status, LocalDateTime from, LocalDateTime to) {
        return entries.stream()
                .filter(e -> service == null || service.isBlank() || e.service().equalsIgnoreCase(service))
                .filter(e -> status == null || e.status() == status)
                .filter(e -> from == null || !e.timestamp().isBefore(from))
                .filter(e -> to == null || !e.timestamp().isAfter(to))
                .sorted(Comparator.comparing(LogEntry::timestamp).reversed())
                .toList();
    }
}
