package co.analisys.gimnasio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;

/**
 * Convierte las excepciones de negocio en respuestas HTTP semánticas
 * para que el unhappy path sea claro (404, 409) en lugar de 500 genérico.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntrenadorNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> entrenadorNoEncontrado(EntrenadorNoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "Entrenador no encontrado",
                        "mensaje", ex.getMessage()
                ));
    }

    @ExceptionHandler(MiembroNoPuedeAsistirException.class)
    public ResponseEntity<Map<String, String>> miembroNoPuedeAsistir(MiembroNoPuedeAsistirException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "No se puede inscribir al miembro",
                        "mensaje", ex.getMessage()
                ));
    }

    @ExceptionHandler(MiembroYaInscritoException.class)
    public ResponseEntity<Map<String, String>> miembroYaInscrito(MiembroYaInscritoException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "Miembro ya inscrito",
                        "mensaje", ex.getMessage()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> capacidadMaxima(IllegalStateException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("capacidad")) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "Capacidad máxima alcanzada",
                            "mensaje", ex.getMessage()
                    ));
        }
        throw ex;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accesoDenegado(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "error", "Forbidden",
                        "mensaje", "No tiene permiso para esta acción"
                ));
    }

    /**
     * Cuando la llamada a entrenador-service o miembro-service devuelve 4xx/5xx,
     * RestTemplate lanza HttpStatusCodeException. Evitamos devolver 500 genérico:
     * 404 del otro servicio -> 404 nuestro; resto -> 502 Bad Gateway.
     */
    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Map<String, String>> errorServicioRemoto(HttpStatusCodeException ex) {
        int status = ex.getStatusCode().value();
        String mensaje = ex.getResponseBodyAsString();
        if (mensaje != null && mensaje.length() > 200) mensaje = mensaje.substring(0, 200);
        if (mensaje == null || mensaje.isBlank()) mensaje = ex.getMessage();
        if (status == 404) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Recurso no encontrado", "mensaje", mensaje != null ? mensaje : "El servicio remoto devolvió 404"));
        }
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "Error en servicio remoto", "mensaje", mensaje != null ? mensaje : "El servicio remoto devolvió " + status));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> otroError(Exception ex) {
        String mensaje = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Internal Server Error",
                        "mensaje", mensaje
                ));
    }
}
