package co.analisys.gimnasio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_INSCRIPCIONES = "gimnasio.inscripciones";

    /** Exchange para pub/sub de eventos del gimnasio (ej. cambio de horario). */
    public static final String EXCHANGE_EVENTOS = "gimnasio.eventos";
    /** Routing key: cambio de horario de una clase. */
    public static final String ROUTING_KEY_HORARIO_CAMBIADO = "clase.horario.cambiado";

    @Bean
    public Queue inscripcionesQueue() {
        return new Queue(QUEUE_INSCRIPCIONES, true);
    }

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
