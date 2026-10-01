package com.octaviookumu.analytics_service.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Slf4j
@Service
public class KafkaConsumer {

    // topics - the topic we want to listen to
    // groupId - tells the kafka broker who this consumer is
    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {

        // convert byte array into java object
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            // ... perform any business logic related to analytics here
            // e.g. calling a repository or database

            log.info("Received Patient Event: [PatientId={}, PatientName={}, PatientEmail={}]",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail());
        } catch (InvalidProtocolBufferException e) {
            // logging instead of throwing an exception, which might cause the whole analytics service to go down
            log.error("Error deserializing event {}", e.getMessage());
        }
    }

}
