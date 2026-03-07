package co.analisys.gimnasio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta deserializada desde MIEMBRO-SERVICE /puede-asistir-clase.
 * Misma estructura que el DTO del servicio de miembros para el RestTemplate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuedeAsistirClaseResponse {
    private boolean puedeAsistir;
    private String razon; // "NO_EXISTE", "MEMBRESIA_INACTIVA" o null
}
