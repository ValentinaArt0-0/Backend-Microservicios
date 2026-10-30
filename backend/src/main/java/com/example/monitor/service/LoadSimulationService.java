package com.example.monitor.service;

import com.example.monitor.model.ServiceException;
import com.example.monitor.proxy.MicroserviceProxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Generates random traffic. Every call goes through the proxies, so every call is logged. */
@Service
public class LoadSimulationService {

    public static final int TOTAL_CALLS = 50;
    private static final List<String> PRODUCTS = List.of("LAPTOP-001", "MOUSE-002", "KEYBOARD-003", "MONITOR-004");

    private final MicroserviceProxy<Object> inventoryProxy;
    private final MicroserviceProxy<Object> orderProxy;
    private final MicroserviceProxy<Object> paymentProxy;
    private final OrderService orderService;

    public LoadSimulationService(@Qualifier("inventoryProxy") MicroserviceProxy<Object> inventoryProxy,
                                 @Qualifier("orderProxy") MicroserviceProxy<Object> orderProxy,
                                 @Qualifier("paymentProxy") MicroserviceProxy<Object> paymentProxy,
                                 OrderService orderService) {
        this.inventoryProxy = inventoryProxy;
        this.orderProxy = orderProxy;
        this.paymentProxy = paymentProxy;
        this.orderService = orderService;
    }

    public Map<String, Object> simulate() {
        int success = 0;
        int errors = 0;
        for (int i = 0; i < TOTAL_CALLS; i++) {
            try {
                runRandomCall();
                success++;
            } catch (ServiceException e) {
                errors++;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCalls", TOTAL_CALLS);
        result.put("successCalls", success);
        result.put("errorCalls", errors);
        result.put("message", "Simulación completada: " + TOTAL_CALLS + " llamadas (" + success
                + " exitosas, " + errors + " con error)");
        return result;
    }

    private void runRandomCall() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String product = PRODUCTS.get(random.nextInt(PRODUCTS.size()));
        switch (random.nextInt(7)) {
            case 0 -> inventoryProxy.execute("getStock", Map.of("productId", product));
            case 1 -> inventoryProxy.execute("addStock", Map.of("productId", product, "quantity", random.nextInt(1, 20)));
            case 2 -> inventoryProxy.execute("removeStock", Map.of("productId", product, "quantity", random.nextInt(1, 40)));
            case 3 -> orderProxy.execute("createOrder", Map.of("productId", product, "quantity", random.nextInt(1, 5)));
            case 4 -> {
                String orderId = orderService.anyOrderId();
                // Occasionally query a missing order to produce a realistic business error.
                if (orderId == null || random.nextInt(8) == 0) {
                    orderId = "ORD-0000";
                }
                orderProxy.execute("getOrder", Map.of("orderId", orderId));
            }
            case 5 -> paymentProxy.execute("processPayment",
                    Map.of("orderId", "ORD-" + random.nextInt(1001, 1100), "amount", random.nextInt(20, 2000)));
            default -> paymentProxy.execute("refundPayment",
                    Map.of("paymentId", "PAY-" + random.nextInt(1000, 9999), "amount", random.nextInt(10, 500)));
        }
    }
}
