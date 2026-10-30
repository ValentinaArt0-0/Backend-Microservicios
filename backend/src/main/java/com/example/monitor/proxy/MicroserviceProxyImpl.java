package com.example.monitor.proxy;

import com.example.monitor.model.LogEntry;
import com.example.monitor.model.LogStatus;
import com.example.monitor.model.ServiceException;
import com.example.monitor.repository.LogRepository;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Intercepts every call to the target service: generates a request id, measures
 * the duration, records SUCCESS/ERROR and stores the log before returning.
 */
public class MicroserviceProxyImpl<T> implements MicroserviceProxy<T> {

    private final ServiceOperation<T> target;
    private final LogRepository logRepository;

    public MicroserviceProxyImpl(ServiceOperation<T> target, LogRepository logRepository) {
        this.target = target;
        this.logRepository = logRepository;
    }

    @Override
    public T execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        long start = System.nanoTime();
        try {
            T result = target.invoke(operation, params);
            logRepository.save(new LogEntry(requestId, target.serviceName(), operation,
                    elapsedMillis(start), LogStatus.SUCCESS, timestamp,
                    "Operación ejecutada correctamente"));
            return result;
        } catch (Exception ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
            logRepository.save(new LogEntry(requestId, target.serviceName(), operation,
                    elapsedMillis(start), LogStatus.ERROR, timestamp, message));
            throw new ServiceException(message, requestId, ex);
        }
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
