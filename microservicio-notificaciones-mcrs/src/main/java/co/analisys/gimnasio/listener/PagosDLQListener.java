package co.analisys.gimnasio.listener;

import co.analisys.gimnasio.config.RabbitMQConfig;
import co.analisys.gimnasio.event.PagoEvent;
import co.analisys.gimnasio.service.PagosFallidosStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume mensajes de la Dead Letter Queue de pagos.
 * Los mensajes que fallaron en PagosProcessorListener llegan aquí.
 * Se guardan en memoria (PagosFallidosStore) para consultarlos en GET /notificaciones/pagos-fallidos.
 */
@Component
public class PagosDLQListener {

    private static final Logger log = LoggerFactory.getLogger(PagosDLQListener.class);

    private final PagosFallidosStore pagosFallidosStore;

    public PagosDLQListener(PagosFallidosStore pagosFallidosStore) {
        this.pagosFallidosStore = pagosFallidosStore;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_PAGOS_DLQ)
    public void recibirPagoFallido(PagoEvent event) {
        log.warn("[DLQ PAGOS] Mensaje de pago fallido recibido (para revisión manual): miembro {} - {} - {} - {}",
                event.getMiembroId(), event.getConcepto(), event.getMonto(), event.getTimestamp());
        pagosFallidosStore.agregar(event);
    }
}
