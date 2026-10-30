package com.example.monitor;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonitorApplication {
    public static void main(String[] args) {
        String zone = System.getenv().getOrDefault("APP_TIMEZONE", "America/Bogota");
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        SpringApplication.run(MonitorApplication.class, args);
    }
}
