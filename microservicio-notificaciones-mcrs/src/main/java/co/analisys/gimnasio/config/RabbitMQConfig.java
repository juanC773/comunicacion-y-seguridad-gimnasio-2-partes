package co.analisys.gimnasio.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_INSCRIPCIONES = "gimnasio.inscripciones";

    /** Pub/sub: exchange y routing key para eventos del gimnasio (debe coincidir con el productor). */
    public static final String EXCHANGE_EVENTOS = "gimnasio.eventos";
    public static final String ROUTING_KEY_HORARIO_CAMBIADO = "clase.horario.cambiado";
    /** Cola propia de notificaciones para el evento cambio de horario (varios suscriptores pueden tener su cola). */
    public static final String QUEUE_CAMBIO_HORARIO = "notificaciones.cambio-horario-clase";

    /** Cola de pagos: mensajes fallidos van a la DLQ. */
    public static final String QUEUE_PAGOS = "gimnasio.pagos";
    public static final String QUEUE_PAGOS_DLQ = "gimnasio.pagos.dlq";
    public static final String EXCHANGE_DLX = "gimnasio.dlx";
    public static final String ROUTING_KEY_PAGOS_DLQ = "pagos.dlq";

    @Bean
    public Queue inscripcionesQueue() {
        return new Queue(QUEUE_INSCRIPCIONES, true);
    }

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(EXCHANGE_EVENTOS, true, false);
    }

    @Bean
    public Queue cambioHorarioQueue() {
        return new Queue(QUEUE_CAMBIO_HORARIO, true);
    }

    @Bean
    public Binding bindingCambioHorario(Queue cambioHorarioQueue, TopicExchange eventosExchange) {
        return BindingBuilder.bind(cambioHorarioQueue).to(eventosExchange).with(ROUTING_KEY_HORARIO_CAMBIADO);
    }

    /** Exchange Dead Letter: recibe mensajes rechazados de la cola de pagos. */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(EXCHANGE_DLX, true, false);
    }

    /** Cola DLQ: mensajes de pago que fallaron tras reintentos. */
    @Bean
    public Queue pagosDLQ() {
        return new Queue(QUEUE_PAGOS_DLQ, true);
    }

    @Bean
    public Binding bindingPagosDLQ(Queue pagosDLQ, DirectExchange dlxExchange) {
        return BindingBuilder.bind(pagosDLQ).to(dlxExchange).with(ROUTING_KEY_PAGOS_DLQ);
    }

    /** Cola de pagos: al fallar el procesamiento, el mensaje se envía al DLX con routing key pagos.dlq. */
    @Bean
    public Queue pagosQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX);
        args.put("x-dead-letter-routing-key", ROUTING_KEY_PAGOS_DLQ);
        return new Queue(QUEUE_PAGOS, true, false, false, args);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
