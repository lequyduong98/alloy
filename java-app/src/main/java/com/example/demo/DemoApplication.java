package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.scheduling.annotation.EnableScheduling; // PHẢI CÓ
import org.springframework.scheduling.annotation.Scheduled;       // PHẢI CÓ
import java.time.LocalDateTime;

@SpringBootApplication
@RestController
@EnableScheduling // Kích hoạt bộ lập lịch tác vụ
public class DemoApplication {
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        log.info("Yêu cầu thủ công tới trang chủ!");
        return "Java App is Running!";
    }

    // Tự động chạy mỗi 5000ms (5 giây)
    @Scheduled(fixedRate = 5000)
    public void generateAutoLogs() {
        log.info("AUTO-LOG: Hệ thống đang hoạt động bình thường tại {}", LocalDateTime.now());
        
        // Giả lập thỉnh thoảng có lỗi để test trên Grafana
        if (Math.random() > 0.8) {
            log.error("AUTO-LOG: Phát hiện lỗi giả lập!");
        }
    }
}