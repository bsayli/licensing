package io.github.bsayli.licensing.api.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {})
@NotBlank(message = "{license.token.required}")
@Size(min = 200, max = 400, message = "{license.token.size}")
@Pattern(
    regexp = "^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$",
    message = "{license.token.format}")
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLicenseToken {

  String message() default "Invalid license token";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
