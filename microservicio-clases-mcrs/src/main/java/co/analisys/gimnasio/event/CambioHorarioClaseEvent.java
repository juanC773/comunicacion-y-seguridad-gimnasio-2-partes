package co.analisys.gimnasio.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioHorarioClaseEvent {

    private String claseId;
    private String claseNombre;
    private LocalDateTime horarioAnterior;
    private LocalDateTime horarioNuevo;
    private Instant timestamp;

    public static CambioHorarioClaseEvent of(String claseId, String claseNombre,
                                             LocalDateTime horarioAnterior, LocalDateTime horarioNuevo) {
        return new CambioHorarioClaseEvent(claseId, claseNombre, horarioAnterior, horarioNuevo, Instant.now());
    }
}
