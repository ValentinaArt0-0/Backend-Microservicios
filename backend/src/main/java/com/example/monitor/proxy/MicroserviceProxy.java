package com.example.monitor.proxy;

/** Generic proxy placed in front of any microservice. */
public interface MicroserviceProxy<T> {
    T execute(String operation, Object... params);
}
