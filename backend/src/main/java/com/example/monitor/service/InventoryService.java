package com.example.monitor.service;

import com.example.monitor.model.ServiceException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class InventoryService extends BaseService {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>(Map.of(
            "LAPTOP-001", 25,
            "MOUSE-002", 120,
            "KEYBOARD-003", 60,
            "MONITOR-004", 15));

    @Override
    public String serviceName() {
        return "InventoryService";
    }

    @Override
    public Object invoke(String operation, Object... params) {
        Map<String, Object> body = body(params);
        simulateLatency(20, 120);
        return switch (operation) {
            case "getStock" -> getStock(text(body, "productId"));
            case "addStock" -> addStock(text(body, "productId"), integer(body, "quantity"));
            case "removeStock" -> removeStock(text(body, "productId"), integer(body, "quantity"));
            default -> throw unsupported(operation);
        };
    }

    private Map<String, Object> getStock(String productId) {
        Integer current = stock.get(productId);
        if (current == null) {
            throw new ServiceException("Producto no encontrado: " + productId);
        }
        return result(productId, current, "Existencias consultadas");
    }

    private Map<String, Object> addStock(String productId, int quantity) {
        int updated = stock.merge(productId, quantity, Integer::sum);
        return result(productId, updated, "Se agregaron " + quantity + " unidades");
    }

    private Map<String, Object> removeStock(String productId, int quantity) {
        if (!stock.containsKey(productId)) {
            throw new ServiceException("Producto no encontrado: " + productId);
        }
        int[] updated = new int[1];
        stock.compute(productId, (key, current) -> {
            if (current < quantity) {
                throw new ServiceException("Existencias insuficientes: disponibles " + current + ", solicitadas " + quantity);
            }
            updated[0] = current - quantity;
            return updated[0];
        });
        return result(productId, updated[0], "Se descontaron " + quantity + " unidades");
    }

    private Map<String, Object> result(String productId, int current, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("stock", current);
        result.put("message", message);
        return result;
    }
}
