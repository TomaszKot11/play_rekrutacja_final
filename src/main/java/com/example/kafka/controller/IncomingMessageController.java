package com.example.kafka.controller;

import com.example.kafka.dto.Sms;
import com.example.kafka.dto.StatusEnum;
import com.example.kafka.service.IncomingMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * "Extracting" Data
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class IncomingMessageController {

    private final IncomingMessageService service;
    private final KafkaTemplate kafkaTemplate;

    /**
     * "Extracting data"
     * Assumptions:
     * START or STOP SMS is always sent to recipient 1234 which represents phone number
     * used for subscription purposes which usually has 4 digit
     */
    @PostMapping("/send")
    public ResponseEntity<StatusEnum> sendMessage(@RequestBody Sms sms) {
        // Mogłoby być z pliku ale jest z schedulera
        log.info("Message: {}", sms.toString());
        service.handleMessage(sms);

        return ResponseEntity.ok(StatusEnum.OK);
    }
}
