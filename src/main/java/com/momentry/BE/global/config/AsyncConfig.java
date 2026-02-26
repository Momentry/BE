package com.momentry.BE.global.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {
    @Value("${ASYNC_CORE_POOL_SIZE:5}") // 환경변수가 없으면 5를 기본값으로 사용
    private int corePoolSize;

    @Value("${ASYNC_MAX_POOL_SIZE:10}")
    private int maxPoolSize;

    @Value("${ASYNC_QUEUE_CAPACITY:100}")
    private int queueCapacity;

    @PostConstruct
    public void checkConfig() {
        log.info("### ASYNC_CORE_POOL_SIZE: {}", corePoolSize);
        log.info("### ASYNC_MAX_POOL_SIZE: {}", maxPoolSize);
        log.info("### ASYNC_QUEUE_CAPACITY: {}", queueCapacity);
    }

    @Bean(name = "s3UploadExecutor")
    public Executor s3UploadExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize); // 기본 스레드 수
        executor.setMaxPoolSize(maxPoolSize); // 최대 스레드 수
        executor.setQueueCapacity(queueCapacity); // 대기 큐
        executor.setThreadNamePrefix("S3-Async-");

        // 거절 정책 설정 -> 비동기 스레드/큐 부족 시 메인 스레드가 위임받아서 적용
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
