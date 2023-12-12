package com.example.kafka.service;

import com.example.kafka.dto.Sms;
import com.example.kafka.repository.UserEntity;
import com.example.kafka.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomingMessageService {

    // TODO: props
    private static final int SUBSCRIPTION_NUMBER = 1234;
    private static final String TOPIC_NAME = "inbound-topic";
    private final UserRepository userRepository;
    private final KafkaTemplate<UUID, String> kafkaTemplate;
    private final ObjectMapper mapper;

    public void handleMessage(Sms sms) {
        if (SUBSCRIPTION_NUMBER == getIntValue(sms.getRecipient())) {
            createUser(sms); // TODO: maybe there is more accurate place to put it
            return;
        }

        sendMessage(TOPIC_NAME, UUID.randomUUID(), getStringMessageValue(sms));
    }

    private Integer getIntValue(String stringNumber) {
        try {
            return Integer.parseInt(stringNumber);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    private UserEntity getUser(Sms sms) {
        Optional<UserEntity> userEntityByNumber = userRepository.findUserEntityByNumber(sms.getSender());
        return userEntityByNumber.orElseGet(() -> createUser(sms));
    }

    public UserEntity createUser(Sms sms) {
        // TODO: should be only valid for START/STOP
        return userRepository.save(UserEntity.builder()
                .number(sms.getSender())
                .isKafkaMessageProcessingEnabled("START".equalsIgnoreCase(sms.getMessage()))
                .createdOn(LocalDateTime.now())
                .build());
    }

    public void sendMessage(String topicName, UUID key, String data) {
        CompletableFuture<SendResult<UUID, String>> future = kafkaTemplate.send(topicName, key, data);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[" + data + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                log.info("Unable to send message=[" + data + "] due to : " + ex.getMessage());
            }
        });
    }

    private String getStringMessageValue(Sms sms) {
        try {
            return mapper.writeValueAsString(sms);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize {}", sms.toString());
            return "error";
        }
    }

    private void handleStartStopSmsMessage(Sms sms) {
        if (SUBSCRIPTION_NUMBER == Integer.parseInt(sms.getRecipient())) {
            UserEntity saved = createUser(sms);
            sendMessage(TOPIC_NAME, saved.getId(), getStringMessageValue(sms));
        }
    }
}
