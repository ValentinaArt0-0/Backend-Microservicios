package com.example.monitor.config;

import com.example.monitor.proxy.MicroserviceProxy;
import com.example.monitor.proxy.MicroserviceProxyImpl;
import com.example.monitor.repository.LogRepository;
import com.example.monitor.service.InventoryService;
import com.example.monitor.service.OrderService;
import com.example.monitor.service.PaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wraps every real service with the auditing proxy. */
@Configuration
public class ProxyConfig {

    @Bean
    public MicroserviceProxy<Object> inventoryProxy(InventoryService service, LogRepository repository) {
        return new MicroserviceProxyImpl<>(service, repository);
    }

    @Bean
    public MicroserviceProxy<Object> orderProxy(OrderService service, LogRepository repository) {
        return new MicroserviceProxyImpl<>(service, repository);
    }

    @Bean
    public MicroserviceProxy<Object> paymentProxy(PaymentService service, LogRepository repository) {
        return new MicroserviceProxyImpl<>(service, repository);
    }
}
