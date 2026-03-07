package co.analisys.gimnasio.exception;

import co.analisys.gimnasio.model.EntrenadorId;

public class EntrenadorNoEncontradoException extends RuntimeException {
    public EntrenadorNoEncontradoException(EntrenadorId entrenadorId) {
        super("Entrenador no encontrado con id: " + entrenadorId.getEntrenadorid_value());
    }
}
