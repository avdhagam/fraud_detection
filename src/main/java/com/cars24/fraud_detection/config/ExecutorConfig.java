package com.cars24.fraud_detection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(10); // Adjust thread count as needed
    }
}
