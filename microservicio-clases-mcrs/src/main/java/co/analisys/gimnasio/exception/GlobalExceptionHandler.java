package co.analisys.gimnasio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
