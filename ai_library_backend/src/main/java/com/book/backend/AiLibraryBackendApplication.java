package com.book.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * AI图书馆后端应用启动类
 */
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class})
@MapperScan("com.book.backend.mapper")
@EnableTransactionManagement
public class AiLibraryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiLibraryBackendApplication.class, args);
    }

}