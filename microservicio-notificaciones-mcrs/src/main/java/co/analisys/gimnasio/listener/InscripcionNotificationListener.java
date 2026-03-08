package co.analisys.gimnasio.listener;

import co.analisys.gimnasio.config.RabbitMQConfig;
import co.analisys.gimnasio.event.InscripcionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InscripcionNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(InscripcionNotificationListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INSCRIPCIONES)
    public void recibirNuevaInscripcion(InscripcionEvent event) {
        log.info("[NOTIFICACIÓN] Nueva inscripción recibida: miembro {} en clase '{}' (id: {}), horario: {}",
                event.getMiembroId(),
                event.getClaseNombre(),
                event.getClaseId(),
                event.getHorarioClase());

        // Aquí se puede extender: enviar email, push, guardar en BD, etc.
        // procesarNotificacion(event);
    }
}
