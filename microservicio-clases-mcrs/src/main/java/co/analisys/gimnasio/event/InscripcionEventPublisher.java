package co.analisys.gimnasio.event;

import co.analisys.gimnasio.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InscripcionEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicarNuevaInscripcion(InscripcionEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_INSCRIPCIONES, event);
    }
}
