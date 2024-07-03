package ru.gpb.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${provided-back-service.url}")
    private String baseUrl;

    @Bean
    public WebClient mainWebClient() {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}

