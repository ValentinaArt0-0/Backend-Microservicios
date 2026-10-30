package com.example.monitor.model;

/** Controlled exception raised by the proxy (or by a service) when an operation fails. */
public class ServiceException extends RuntimeException {
    private final String requestId;

    public ServiceException(String message, String requestId, Throwable cause) {
        super(message, cause);
        this.requestId = requestId;
    }

    public ServiceException(String message) {
        this(message, null, null);
    }

    public String getRequestId() {
        return requestId;
    }
}
