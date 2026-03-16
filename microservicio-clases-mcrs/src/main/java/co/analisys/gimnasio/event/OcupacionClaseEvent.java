package co.analisys.gimnasio.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcupacionClaseEvent {

    private String claseId;
    private String claseNombre;
    private int ocupacionActual;
    private int capacidadMaxima;
    private LocalDateTime timestamp;

    public static OcupacionClaseEvent of(String claseId, String claseNombre,
                                         int ocupacionActual, int capacidadMaxima) {
        return new OcupacionClaseEvent(claseId, claseNombre, ocupacionActual, capacidadMaxima, LocalDateTime.now());
    }
}
