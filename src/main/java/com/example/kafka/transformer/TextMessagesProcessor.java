package com.example.kafka.transformer;

import com.example.kafka.dto.Sms;
import com.example.kafka.repository.UserEntity;
import com.example.kafka.repository.UserRepository;
import com.example.kafka.service.IncomingMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.webrisk.v1.WebRiskServiceClient;
import com.google.webrisk.v1.SearchUrisResponse;
import com.google.webrisk.v1.ThreatType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TextMessagesProcessor {
    // TODO: take from props
    private final String INBOUD_TOPIC = "inbound-topic";
    private final String OUTBOUND_TOPIC = "outbound-topic";
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final WebRiskServiceClient webRiskServiceClient;
    private final IncomingMessageService service;
    private final StreamsBuilder streamsBuilder;

    @PostConstruct
    public KStream<UUID, String> streamTopology() {
        KStream<UUID, String> inboudTopicStream = streamsBuilder.stream(INBOUD_TOPIC, Consumed.with(Serdes.UUID(), Serdes.String()));
        KStream<UUID, String> filter = inboudTopicStream.filter((key, value) -> { // TODO: lambda could be extracted
            Sms sms = null;
            try { // TODO: should be better written
                sms = objectMapper.readValue(value, Sms.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            // TODO: is this really a best move to do this that way?
            UserEntity entity = getUser(sms);

            if (entity.getIsKafkaMessageProcessingEnabled()) {
                String url = sms.getUrl(); // only null when deserialization fails - should be handled
                // possible caching here of the url if it was already marked as a phising one
                List<ThreatType> threatTypes = List.of(ThreatType.SOCIAL_ENGINEERING:);
                SearchUrisResponse response = webRiskServiceClient.searchUris(url, threatTypes);
                log.info("Response#hasThreat returned: {}", response.hasThreat());

               return response.hasThreat();
            }

            return false;
        });

        filter.to(OUTBOUND_TOPIC, Produced.with(Serdes.UUID(), Serdes.String()));
        return filter;
    }

    // TODO: this should not be here
    private UserEntity getUser(Sms sms) {
        Optional<UserEntity> userEntityByNumber = userRepository.findUserEntityByNumber(sms.getSender()); // TODO: receipent?
        return userEntityByNumber.orElseGet(() -> service.createUser(sms));
    }
}
