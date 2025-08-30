package com.itau.ingestao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
public class SqsConfig {

    @Value("${aws.sqs.endpoint}")
    private String endpointQueue;

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.SA_EAST_1)
                .endpointOverride(URI.create(endpointQueue))
                .credentialsProvider(() -> AwsBasicCredentials.create("test", "test"))
                .build();
    }

}
