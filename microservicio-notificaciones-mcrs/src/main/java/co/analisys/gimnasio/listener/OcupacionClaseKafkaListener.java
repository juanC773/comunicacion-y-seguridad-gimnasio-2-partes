package co.analisys.gimnasio.listener;

import co.analisys.gimnasio.event.OcupacionClaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OcupacionClaseKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OcupacionClaseKafkaListener.class);

    @KafkaListener(topics = "ocupacion-clases", groupId = "monitoreo-grupo")
    public void consumirOcupacion(OcupacionClaseEvent event) {
        log.info("[KAFKA] Ocupación actualizada: clase '{}' ({}) → {}/{}",
                event.getClaseNombre(),
                event.getClaseId(),
                event.getOcupacionActual(),
                event.getCapacidadMaxima());
    }
}
