package backend.web.exception;

import backend.domain.exception.OperacionInvalidaException;
import backend.domain.exception.RecursoNoEncontradoException;
import backend.domain.exception.ServicioExternoNoDisponibleException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador global de errores en formato RFC 7807 ([ProblemDetail]).
 * Nunca expone nombres internos de tablas, métodos Java, consultas SQL ni stacktraces al cliente (OWASP).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Relanza excepciones de seguridad para que Spring Security responda con 401/403 (y no 500). */
    @ExceptionHandler(AccessDeniedException.class)
    public void rethrowAccessDenied(AccessDeniedException ex) {
        throw ex;
    }

    @ExceptionHandler(AuthenticationException.class)
    public void rethrowAuthentication(AuthenticationException ex) {
        throw ex;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Algunos campos enviados no son válidos.");
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errores.putIfAbsent(err.getField(), err.getDefaultMessage()));
        detail.setProperty("errores", errores);
        return detail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Los parámetros enviados no cumplen con el formato esperado.");
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String ruta = v.getPropertyPath().toString();
            int ultimoPunto = ruta.lastIndexOf('.');
            String parametro = ultimoPunto >= 0 ? ruta.substring(ultimoPunto + 1) : ruta;
            errores.putIfAbsent(parametro, v.getMessage());
        });
        detail.setProperty("errores", errores);
        return detail;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Los parámetros enviados no cumplen con el formato esperado.");
    }

    @ExceptionHandler({
        OperacionInvalidaException.class,
        IllegalArgumentException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class
    })
    public ProblemDetail handleBadRequest(Exception ex) {
        String mensaje = ex instanceof OperacionInvalidaException
                ? ex.getMessage()
                : "Solicitud inválida. Verifica los parámetros o el cuerpo JSON enviado.";
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler({RecursoNoEncontradoException.class, NoResourceFoundException.class})
    public ProblemDetail handleNotFound(Exception ex) {
        String mensaje = ex instanceof RecursoNoEncontradoException
                ? ex.getMessage()
                : "El recurso solicitado no existe.";
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, mensaje);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.METHOD_NOT_ALLOWED,
                "El método HTTP utilizado no está permitido en esta ruta.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de contenido no soportado.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handlePayloadTooLarge(MaxUploadSizeExceededException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONTENT_TOO_LARGE,
                "El archivo enviado excede el tamaño máximo permitido (15 MB).");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Conflicto de integridad de datos procesando solicitud: {}", ex.getMostSpecificCause().getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "No se pudo completar la operación por conflicto o referencia inexistente.");
    }

    @ExceptionHandler(ServicioExternoNoDisponibleException.class)
    public ProblemDetail handleExternalUnavailable(ServicioExternoNoDisponibleException ex) {
        log.warn("Servicio externo no disponible: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de datos externos no está disponible en este momento. Intenta de nuevo más tarde.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Error inesperado procesando la solicitud", ex);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Intenta nuevamente más tarde.");
    }
}
