package com.octaviookumu.patient_service.kafka;

import com.octaviookumu.patient_service.domain.entities.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Slf4j
@Service
public class KafkaProducer {

    // how we define message types, key - string and value - byte array
    private final KafkaTemplate<String, byte[]> kafkaTemplate;


    public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient) {
        // create an event that has the properties
        PatientEvent event = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED") // can be used to add subcategories to a message in a topic
                .build();

        // send event using kafka template
        // because it's in a try catch block, the app won't crash
        try {
            // we convert the event to a byte array to keep the size of the message down
            // to convert the message to an object in the consumer code
            kafkaTemplate.send("patient", event.toByteArray());
        } catch (Exception e) {
            log.error("Error sending PatientCreated event {}", event);
        }

    }

}
