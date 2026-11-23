package backend.domain.exception;

/** Se lanza cuando una API pública externa no responde y tampoco hay dato en caché local. */
public class ServicioExternoNoDisponibleException extends RuntimeException {

    public ServicioExternoNoDisponibleException(String message) {
        super(message);
    }

    public ServicioExternoNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
