package com.meksula.nbp.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Value("${nbp-api.root-url}")
    private String rootUri;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
//                .rootUri(rootUri)
                .build();
    }
}
