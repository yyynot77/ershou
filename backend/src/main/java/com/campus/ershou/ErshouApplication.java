package com.campus.ershou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.campus.ershou.mapper")
@EnableScheduling
public class ErshouApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErshouApplication.class, args);
    }
}
