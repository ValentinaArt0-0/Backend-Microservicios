package com.example.monitor.service;

import com.example.monitor.model.ServiceException;
import com.example.monitor.proxy.ServiceOperation;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Shared helpers for the simulated services. */
public abstract class BaseService implements ServiceOperation<Object> {

    /** Simulates network / processing latency so the dashboard has varied durations. */
    protected void simulateLatency(int minMillis, int maxMillis) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(minMillis, maxMillis + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> body(Object... params) {
        if (params != null && params.length > 0 && params[0] instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    protected String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new ServiceException("Falta el parámetro obligatorio: " + key);
        }
        return value.toString().trim();
    }

    protected int integer(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new ServiceException("Falta el parámetro obligatorio: " + key);
        }
        try {
            int number = (value instanceof Number n) ? n.intValue() : Integer.parseInt(value.toString().trim());
            if (number <= 0) {
                throw new ServiceException("El parámetro " + key + " debe ser mayor que cero");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new ServiceException("El parámetro " + key + " debe ser numérico");
        }
    }

    protected double decimal(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null) {
            throw new ServiceException("Falta el parámetro obligatorio: " + key);
        }
        try {
            double number = (value instanceof Number n) ? n.doubleValue() : Double.parseDouble(value.toString().trim());
            if (number <= 0) {
                throw new ServiceException("El parámetro " + key + " debe ser mayor que cero");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new ServiceException("El parámetro " + key + " debe ser numérico");
        }
    }

    protected boolean flag(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value != null && Boolean.parseBoolean(value.toString());
    }

    protected ServiceException unsupported(String operation) {
        return new ServiceException("Operación no soportada: " + operation);
    }
}
