package co.analisys.gimnasio.exception;

public class MiembroYaInscritoException extends RuntimeException {
    public MiembroYaInscritoException() {
        super("El miembro ya está inscrito en esta clase.");
    }
}
