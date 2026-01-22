package com.example.demo;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.UUID;

@SpringBootApplication
@RestController
@EnableScheduling
public class DemoApplication {
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    // Sử dụng constructor injection hoặc gọi trực tiếp từ context để tránh Circular Dependency
    private final RestTemplate restTemplate;

    public DemoApplication(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        String orderId = UUID.randomUUID().toString();
        log.info("Yêu cầu thủ công - OrderID: {}", orderId);
        processOrder(orderId);
        return "Order " + orderId + " is being processed!";
    }

    @Scheduled(fixedRate = 15000)
    public void scheduledTask() {
        String orderId = "AUTO-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("--- KHỞI CHẠY QUY TRÌNH TỰ ĐỘNG: {} ---", orderId);
        try {
            processOrder(orderId);
        } catch (Exception e) {
            log.error("Lỗi quy trình tổng thể cho đơn hàng {}", orderId);
        }
    }

    @WithSpan("MainOrderFlow") 
    private void processOrder(String orderId) {
        // Tag quan trọng để tìm kiếm trên Grafana
        Span currentSpan = Span.current();
        currentSpan.setAttribute("order.id", orderId);
        currentSpan.setAttribute("order.type", orderId.startsWith("AUTO") ? "AUTOMATIC" : "MANUAL");
        
        checkInventory();
        
        if (Math.random() > 0.2) {
            validateCustomerWithNodeJS();
        }

        processPayment(orderId);
        sendNotification();
        
        log.info("Hoàn tất đơn hàng: {}", orderId);
    }

    @WithSpan("Step1_InventoryCheck")
    private void checkInventory() {
        sleep(150);
        log.info("Kiểm tra kho thành công.");
    }

    @WithSpan("Step2_ExternalCall_NodeJS")
    private void validateCustomerWithNodeJS() {
        try {
            // URL gọi sang container Node.js trong Docker Network
            String response = restTemplate.getForObject("http://nodejs-app:3000/validate", String.class);
            log.info("NodeJS xác nhận khách hàng: {}", response);
        } catch (Exception e) {
            log.warn("Không thể kết nối với NodeJS-App, bỏ qua bước xác thực.");
            Span.current().setAttribute("error.message", "NodeJS Service Unavailable");
        }
    }

    @WithSpan("Step3_PaymentGateway")
    private void processPayment(String orderId) {
        sleep(400);
        if (Math.random() > 0.85) { // 15% tỉ lệ lỗi
            log.error("Thanh toán thất bại cho đơn {}", orderId);
            // Ghi lỗi trực tiếp vào Trace để hiện màu đỏ trên Grafana
            Span.current().recordException(new RuntimeException("Payment Gateway Timeout"));
            throw new RuntimeException("Payment Failed");
        }
        log.info("Thanh toán hoàn tất.");
    }

    @WithSpan("Step4_NotificationService")
    private void sendNotification() {
        sleep(100);
        Span.current().addEvent("Email Sent Successfully");
        log.info("Thông báo đã gửi.");
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}

// Tách cấu hình Bean ra một Class riêng để triệt tiêu lỗi Circular Reference
@Configuration
class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}