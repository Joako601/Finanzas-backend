package backend.web.validation;

import backend.domain.validation.ValidadorUrlHttps;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UrlHttpsSeguraValidator implements ConstraintValidator<UrlHttpsSegura, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return ValidadorUrlHttps.esValidaOOpcional(value);
    }
}
