package co.analisys.gimnasio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta del endpoint puede-asistir-clase para que el cliente
 * sepa la razón cuando no puede asistir (no existe, membresía inactiva).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuedeAsistirClaseResponse {
    private boolean puedeAsistir;
    /** Cuando puedeAsistir es false: "NO_EXISTE" o "MEMBRESIA_INACTIVA". Cuando true: null. */
    private String razon;
}
