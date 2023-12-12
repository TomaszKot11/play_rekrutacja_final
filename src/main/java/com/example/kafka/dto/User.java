package com.example.kafka.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class User {
    private UUID uuid;
    private Boolean isKafkaMessageProcessingEnabled;
}
