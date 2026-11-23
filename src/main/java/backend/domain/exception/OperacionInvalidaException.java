package backend.domain.exception;

/** Se lanza cuando los parámetros de entrada incumplen una regla de negocio del dominio. */
public class OperacionInvalidaException extends RuntimeException {

    public OperacionInvalidaException(String message) {
        super(message);
    }
}
