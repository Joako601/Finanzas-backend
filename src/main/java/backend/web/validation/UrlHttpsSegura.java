package backend.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Valida que una URL opcional use HTTPS hacia un dominio público válido, sin credenciales ni control chars. */
@Documented
@Constraint(validatedBy = UrlHttpsSeguraValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlHttpsSegura {
    String message() default "La URL de imagen debe ser una dirección HTTPS pública válida.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
