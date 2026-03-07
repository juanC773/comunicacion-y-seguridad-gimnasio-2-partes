package co.analisys.gimnasio.exception;

import co.analisys.gimnasio.model.MiembroId;

public class MiembroNoPuedeAsistirException extends RuntimeException {
    public MiembroNoPuedeAsistirException(MiembroId miembroId) {
        super("Miembro no encontrado o sin membresía activa: " + miembroId.getMiembroid_value());
    }

    /** Mensaje específico para el usuario (no existe, membresía inactiva, etc.). */
    public MiembroNoPuedeAsistirException(String mensaje) {
        super(mensaje);
    }
}
