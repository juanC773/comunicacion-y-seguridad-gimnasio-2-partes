package co.analisys.gimnasio.listener;

import co.analisys.gimnasio.config.RabbitMQConfig;
import co.analisys.gimnasio.event.CambioHorarioClaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CambioHorarioClaseListener {

    private static final Logger log = LoggerFactory.getLogger(CambioHorarioClaseListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CAMBIO_HORARIO)
    public void recibirCambioHorario(CambioHorarioClaseEvent event) {
        log.info("[NOTIFICACIÓN] Cambio de horario de clase: '{}' (id: {}), de {} a {}",
                event.getClaseNombre(),
                event.getClaseId(),
                event.getHorarioAnterior(),
                event.getHorarioNuevo());

        // Aquí se puede notificar a miembros inscritos (email, push, etc.)
    }
}
