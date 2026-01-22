package com.example.demo;

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
import java.time.LocalDateTime;

@SpringBootApplication
@RestController
@EnableScheduling
public class DemoApplication {
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @GetMapping("/")
    public String hello() {
        log.info("Yêu cầu thủ công tới trang chủ!");
        processOrder(); // Gọi quy trình phức tạp
        return "Java App is Running with Traces!";
    }

    // Tự động chạy mỗi 10 giây để tạo dữ liệu liên tục
    @Scheduled(fixedRate = 10000)
    public void scheduledTask() {
        log.info("--- KHỞI CHẠY QUY TRÌNH TỰ ĐỘNG ---");
        try {
            processOrder();
        } catch (Exception e) {
            log.error("Lỗi quy trình tổng thể", e);
        }
    }

    // --- GIẢ LẬP QUY TRÌNH NGHIỆP VỤ NHIỀU BƯỚC ---

    private void processOrder() {
        log.info("BƯỚC 1: Kiểm tra kho hàng...");
        checkInventory();

        log.info("BƯỚC 2: Xử lý thanh toán...");
        if (Math.random() > 0.1) { // 90% thành công
            processPayment();
        } else {
            log.warn("CẢNH BÁO: Thanh toán bị chậm, đang thử lại...");
            processPayment();
        }

        log.info("BƯỚC 3: Gửi thông báo...");
        sendNotification();
    }

    private void checkInventory() {
        sleep(200); // Giả lập độ trễ 200ms
        log.info("Kho hàng: Còn 50 sản phẩm.");
    }

    private void processPayment() {
        sleep(500);
        double chance = Math.random();
        if (chance > 0.85) { // 15% gây lỗi nặng
            log.error("LỖI: Cổng thanh toán từ chối giao dịch!");
            throw new RuntimeException("Payment Gateway Timeout");
        }
        log.info("Thanh toán thành công.");
    }

    private void sendNotification() {
        sleep(100);
        log.info("Thông báo đã được gửi tới khách hàng qua Email.");
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { }
    }
}