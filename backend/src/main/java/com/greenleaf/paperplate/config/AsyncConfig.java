package com.greenleaf.paperplate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot auto-configures thread pool from application.properties
    // spring.task.execution.pool.* settings apply here
}
