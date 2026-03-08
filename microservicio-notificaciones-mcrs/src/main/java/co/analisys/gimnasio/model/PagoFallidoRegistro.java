package co.analisys.gimnasio.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Registro de un pago que falló y llegó a la DLQ (guardado en memoria para consulta).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoFallidoRegistro {

    private String miembroId;
    private String concepto;
    private BigDecimal monto;
    private Instant timestampPago;   // del mensaje original
    private Instant timestampRecibidoDLQ;  // cuándo llegó a la DLQ
}
