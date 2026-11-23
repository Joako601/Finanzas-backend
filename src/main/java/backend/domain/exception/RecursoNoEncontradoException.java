package backend.domain.exception;

/** Se lanza cuando un recurso solicitado no existe o está inactivo. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
