package com.example.monitor.service;

import com.example.monitor.model.ServiceException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
public class PaymentService extends BaseService {

    private static final double FAILURE_PROBABILITY = 0.10;

    @Override
    public String serviceName() {
        return "PaymentService";
    }

    @Override
    public Object invoke(String operation, Object... params) {
        Map<String, Object> body = body(params);
        simulateLatency(40, 250);
        return switch (operation) {
            case "processPayment" -> processPayment(body);
            case "refundPayment" -> refundPayment(body);
            default -> throw unsupported(operation);
        };
    }

    private Map<String, Object> processPayment(Map<String, Object> body) {
        String orderId = text(body, "orderId");
        double amount = decimal(body, "amount");
        // forceError lets the demo trigger a failure on demand; otherwise 10% random failure.
        if (flag(body, "forceError") || ThreadLocalRandom.current().nextDouble() < FAILURE_PROBABILITY) {
            throw new ServiceException("Fallo simulado: la pasarela de pagos rechazó la transacción");
        }
        Map<String, Object> payment = new LinkedHashMap<>();
        payment.put("paymentId", "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        payment.put("orderId", orderId);
        payment.put("amount", amount);
        payment.put("status", "APPROVED");
        return payment;
    }

    private Map<String, Object> refundPayment(Map<String, Object> body) {
        String paymentId = text(body, "paymentId");
        double amount = decimal(body, "amount");
        if (flag(body, "forceError")) {
            throw new ServiceException("Fallo simulado: no fue posible reembolsar el pago " + paymentId);
        }
        Map<String, Object> refund = new LinkedHashMap<>();
        refund.put("refundId", "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        refund.put("paymentId", paymentId);
        refund.put("amount", amount);
        refund.put("status", "REFUNDED");
        return refund;
    }
}
