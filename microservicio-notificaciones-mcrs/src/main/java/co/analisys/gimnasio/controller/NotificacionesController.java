package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.config.RabbitMQConfig;
import co.analisys.gimnasio.event.PagoEvent;
import co.analisys.gimnasio.model.PagoFallidoRegistro;
import co.analisys.gimnasio.service.PagosFallidosStore;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Endpoints del servicio de notificaciones.
 * POST /notificaciones/simular-pago: envía un mensaje a la cola de pagos (para demostrar DLQ).
 * GET /notificaciones/pagos-fallidos: lista los pagos que llegaron a la DLQ (guardados en memoria).
 */
@RestController
@RequestMapping("/notificaciones")
public class NotificacionesController {

    private final RabbitTemplate rabbitTemplate;
    private final PagosFallidosStore pagosFallidosStore;

    public NotificacionesController(RabbitTemplate rabbitTemplate, PagosFallidosStore pagosFallidosStore) {
        this.rabbitTemplate = rabbitTemplate;
        this.pagosFallidosStore = pagosFallidosStore;
    }

    @GetMapping("/public/status")
    public String getPublicStatus() {
        return "El servicio de notificaciones está funcionando correctamente";
    }

    /**
     * Simula el envío de un pago a la cola. El consumidor falla a propósito y el mensaje va a la DLQ.
     * Body JSON: { "miembroId": "1", "concepto": "membresía", "monto": 50.00 }
     */
    @PostMapping("/simular-pago")
    public ResponseEntity<String> simularPago(@RequestBody(required = false) SimularPagoRequest request) {
        if (request == null) {
            request = new SimularPagoRequest();
        }
        if (request.getMiembroId() == null) request.setMiembroId("1");
        if (request.getConcepto() == null) request.setConcepto("membresía");
        if (request.getMonto() == null) request.setMonto(new BigDecimal("50.00"));
        PagoEvent event = PagoEvent.of(request.getMiembroId(), request.getConcepto(), request.getMonto());
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_PAGOS, event);
        return ResponseEntity.ok("Mensaje de pago enviado a la cola. Será procesado y enviado a la DLQ por fallo simulado.");
    }

    /**
     * Lista los pagos que fallaron y llegaron a la DLQ (guardados en memoria).
     * Útil para ver qué mensajes hay que revisar o reprocesar.
     */
    @GetMapping("/pagos-fallidos")
    public List<PagoFallidoRegistro> listarPagosFallidos() {
        return pagosFallidosStore.listar();
    }
}
