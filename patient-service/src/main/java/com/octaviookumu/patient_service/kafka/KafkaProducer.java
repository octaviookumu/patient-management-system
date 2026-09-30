package com.octaviookumu.patient_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {

    // how we define message types, key - string and value - byte array
    private final KafkaTemplate<String, byte[]> kafkaTemplate;


    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
}
