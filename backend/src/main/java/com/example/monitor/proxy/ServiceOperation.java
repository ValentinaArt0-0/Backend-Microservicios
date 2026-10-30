package com.example.monitor.proxy;

/** Contract every real service implements so the proxy can delegate to it. */
public interface ServiceOperation<T> {
    String serviceName();

    T invoke(String operation, Object... params);
}
