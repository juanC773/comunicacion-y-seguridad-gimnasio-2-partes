package co.analisys.gimnasio.event;

import co.analisys.gimnasio.config.KafkaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OcupacionClaseProducer {

    @Autowired
    private KafkaTemplate<String, OcupacionClaseEvent> kafkaTemplate;

    public void publicarOcupacion(OcupacionClaseEvent event) {
        String key = event.getClaseId() != null ? event.getClaseId() : "sin-id";
        kafkaTemplate.send(KafkaConfig.TOPIC_OCUPACION_CLASES, key, event);
    }
}
