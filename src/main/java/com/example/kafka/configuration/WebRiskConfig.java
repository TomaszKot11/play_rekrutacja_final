package com.example.kafka.configuration;

import com.google.cloud.webrisk.v1.WebRiskServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class WebRiskConfig {
    @Bean
    public WebRiskServiceClient webRiskSErviceClient() throws IOException {
        return WebRiskServiceClient.create();
    }
}
