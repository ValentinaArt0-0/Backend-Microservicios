package com.example.monitor.service;

import com.example.monitor.model.ServiceException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class OrderService extends BaseService {

    private final Map<String, Map<String, Object>> orders = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    @Override
    public String serviceName() {
        return "OrderService";
    }

    @Override
    public Object invoke(String operation, Object... params) {
        Map<String, Object> body = body(params);
        simulateLatency(30, 180);
        return switch (operation) {
            case "createOrder" -> createOrder(text(body, "productId"), integer(body, "quantity"));
            case "getOrder" -> getOrder(text(body, "orderId"));
            default -> throw unsupported(operation);
        };
    }

    private Map<String, Object> createOrder(String productId, int quantity) {
        String orderId = "ORD-" + sequence.incrementAndGet();
        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderId", orderId);
        order.put("productId", productId);
        order.put("quantity", quantity);
        order.put("status", "CREATED");
        order.put("createdAt", LocalDateTime.now().toString());
        orders.put(orderId, order);
        return order;
    }

    private Map<String, Object> getOrder(String orderId) {
        Map<String, Object> order = orders.get(orderId);
        if (order == null) {
            throw new ServiceException("Pedido no encontrado: " + orderId);
        }
        return order;
    }

    /** Used by the load simulator to pick an existing order id (or null when none exist). */
    public String anyOrderId() {
        return orders.keySet().stream().findAny().orElse(null);
    }
}
