package com.meksula.nbp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
class RestTemplateConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder,
                              @Value("${nbp-api.connect-timeout}") Duration connectTimeout,
                              @Value("${nbp-api.read-timeout}") Duration readTimeout) {
        return builder
                .setConnectTimeout(connectTimeout)
                .setReadTimeout(readTimeout)
                .build();
    }
}
