package co.analisys.gimnasio.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InscripcionEvent {

    private String claseId;
    private String claseNombre;
    private String miembroId;
    private LocalDateTime horarioClase;
    private Instant timestamp;

    public static InscripcionEvent of(String claseId, String claseNombre, String miembroId, LocalDateTime horarioClase) {
        return new InscripcionEvent(claseId, claseNombre, miembroId, horarioClase, Instant.now());
    }
}
