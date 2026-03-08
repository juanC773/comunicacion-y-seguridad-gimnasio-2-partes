package co.analisys.gimnasio.event;

import co.analisys.gimnasio.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventosGimnasioPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicarCambioHorarioClase(CambioHorarioClaseEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_EVENTOS,
                RabbitMQConfig.ROUTING_KEY_HORARIO_CAMBIADO,
                event
        );
    }
}
