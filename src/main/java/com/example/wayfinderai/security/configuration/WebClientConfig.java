package com.example.wayfinderai.security.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient fastapiWebClient() {
        // FastAPI 서버가 http://localhost:8000 에서 실행 중이라고 가정
        return WebClient.builder().baseUrl("http://localhost:8000").build();
    }
}