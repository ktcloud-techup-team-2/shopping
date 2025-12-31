package com.kt.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
@RequiredArgsConstructor
public class SesConfig {

    private final AwsProperties awsProperties;

    @Bean
    public SesClient amazonSimpleEmailService() {
        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        awsProperties.getSes().getAccessKey(),
                        awsProperties.getSes().getSecretKey()
                );

        return SesClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}