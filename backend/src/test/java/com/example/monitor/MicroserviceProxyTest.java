package com.example.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.monitor.model.LogEntry;
import com.example.monitor.model.LogStatus;
import com.example.monitor.model.ServiceException;
import com.example.monitor.proxy.MicroserviceProxy;
import com.example.monitor.proxy.MicroserviceProxyImpl;
import com.example.monitor.repository.LogRepository;
import com.example.monitor.service.PaymentService;
import com.example.monitor.service.InventoryService;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MicroserviceProxyTest {

    @Test
    void successfulCallIsLogged() {
        LogRepository repository = new LogRepository();
        MicroserviceProxy<Object> proxy = new MicroserviceProxyImpl<>(new InventoryService(), repository);

        proxy.execute("getStock", Map.of("productId", "LAPTOP-001"));

        LogEntry entry = repository.findAll().get(0);
        assertEquals(LogStatus.SUCCESS, entry.status());
        assertEquals("InventoryService", entry.service());
        assertEquals("getStock", entry.operation());
        assertEquals(36, entry.requestId().length());
    }

    @Test
    void failedCallIsLoggedAndRethrown() {
        LogRepository repository = new LogRepository();
        MicroserviceProxy<Object> proxy = new MicroserviceProxyImpl<>(new PaymentService(), repository);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> proxy.execute("processPayment", Map.of("orderId", "ORD-1", "amount", 10, "forceError", true)));

        assertNotNull(ex.getRequestId());
        assertEquals(LogStatus.ERROR, repository.findAll().get(0).status());
    }
}
