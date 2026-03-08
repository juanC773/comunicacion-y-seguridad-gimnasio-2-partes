package co.analisys.gimnasio.listener;

import co.analisys.gimnasio.config.RabbitMQConfig;
import co.analisys.gimnasio.event.PagoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Procesa mensajes de la cola de pagos. Para demostrar la DLQ,
 * lanza excepción de forma que el mensaje sea rechazado y enviado a gimnasio.pagos.dlq.
 */
@Component
public class PagosProcessorListener {

    private static final Logger log = LoggerFactory.getLogger(PagosProcessorListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAGOS)
    public void procesarPago(PagoEvent event) {
        log.info("[PAGOS] Procesando pago: miembro {} - {} - {}", event.getMiembroId(), event.getConcepto(), event.getMonto());

        // Simulación: fallar siempre para que el mensaje vaya a la DLQ (demostración).
        // En producción aquí iría la lógica real (gateway de pago, etc.).
        throw new RuntimeException("Simulación de fallo en procesamiento de pago (para demostrar DLQ)");
    }
}
