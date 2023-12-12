package com.example.kafka.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", schema="public")
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(nullable = false)
    private String number;
    @Column(nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "kafka_enabled", nullable = false)
    private Boolean isKafkaMessageProcessingEnabled;
}
