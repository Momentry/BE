package com. momentry. BE. global. config;

import io.awspring.cloud.sqs.config.SqsBootstrapConfiguration;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
@Import(SqsBootstrapConfiguration.class)
public class SqsConfig {

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Bean("defaultSqsListenerContainerFactory")
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        SqsMessageListenerContainerFactory<Object> factory = new SqsMessageListenerContainerFactory<>();
        factory.setSqsAsyncClient(sqsAsyncClient);

        // 3.x에서는 configure 안에서 모든 핵심 설정을 처리합니다.
        factory.configure(options -> options
                .acknowledgementMode(AcknowledgementMode.ON_SUCCESS)
                // 컴파일 에러를 피하기 위해 옵션 빌더를 통해 컨버터 등록
                .messageConverter(new SqsMessagingMessageConverter())
        );

        return factory;
    }

    // JSON 변환을 담당하는 기본 메시지 컨버터 빈 등록
    @Bean
    public SqsMessagingMessageConverter sqsMessagingMessageConverter() {
        return new SqsMessagingMessageConverter();
    }
}