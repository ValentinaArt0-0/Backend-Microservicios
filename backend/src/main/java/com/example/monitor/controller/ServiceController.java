package com.example.monitor.controller;

import com.example.monitor.proxy.MicroserviceProxy;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Execution endpoints. Controllers never call the services directly: only through the proxies. */
@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final MicroserviceProxy<Object> inventoryProxy;
    private final MicroserviceProxy<Object> orderProxy;
    private final MicroserviceProxy<Object> paymentProxy;

    public ServiceController(@Qualifier("inventoryProxy") MicroserviceProxy<Object> inventoryProxy,
                             @Qualifier("orderProxy") MicroserviceProxy<Object> orderProxy,
                             @Qualifier("paymentProxy") MicroserviceProxy<Object> paymentProxy) {
        this.inventoryProxy = inventoryProxy;
        this.orderProxy = orderProxy;
        this.paymentProxy = paymentProxy;
    }

    @PostMapping("/inventory/{operation}")
    public Object inventory(@PathVariable String operation, @RequestBody(required = false) Map<String, Object> body) {
        return inventoryProxy.execute(operation, safe(body));
    }

    @PostMapping("/orders/{operation}")
    public Object orders(@PathVariable String operation, @RequestBody(required = false) Map<String, Object> body) {
        return orderProxy.execute(operation, safe(body));
    }

    @PostMapping("/payments/{operation}")
    public Object payments(@PathVariable String operation, @RequestBody(required = false) Map<String, Object> body) {
        return paymentProxy.execute(operation, safe(body));
    }

    private Map<String, Object> safe(Map<String, Object> body) {
        return body == null ? Map.of() : body;
    }
}
