package co.analisys.gimnasio.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoEvent {

    private String miembroId;
    private String concepto;   // ej. "membresía", "clase"
    private BigDecimal monto;
    private Instant timestamp;

    public static PagoEvent of(String miembroId, String concepto, BigDecimal monto) {
        return new PagoEvent(miembroId, concepto, monto, Instant.now());
    }
}
