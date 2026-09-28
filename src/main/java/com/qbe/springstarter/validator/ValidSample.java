package com.qbe.springstarter.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = com.qbe.springstarter.validator.SampleDtoValidator.class)
@Documented
public @interface ValidSample {

    String message() default "Sample is not consistent";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
