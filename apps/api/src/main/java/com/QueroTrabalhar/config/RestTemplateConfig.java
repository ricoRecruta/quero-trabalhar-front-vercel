package com.QueroTrabalhar.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${google.maps.connect-timeout-ms}")
    private long connectTimeoutMs;

    @Value("${google.maps.read-timeout-ms}")
    private long readTimeoutMs;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.connectTimeout(Duration.ofMillis(connectTimeoutMs)).readTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
