package com.qbe.springstarter.validator;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.enums.Status;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SampleDtoValidator implements ConstraintValidator<ValidSample, SampleDto> {

    @Override
    public boolean isValid(SampleDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        // Règle 1
        if (Boolean.TRUE.equals(dto.active()) && (dto.stock() == null || dto.stock() <= 0)) {
            context.buildConstraintViolationWithTemplate("An active product must have stock")
                    .addPropertyNode("stock")
                    .addConstraintViolation();
            return false;
        }

        // Règle 2
        if (dto.status() == Status.DELETED && dto.stock() != null && dto.stock() > 0) {
            context.buildConstraintViolationWithTemplate("A deleted product cannot have stock")
                    .addPropertyNode("stock")
                    .addConstraintViolation();
            return false;
        }

        // Règle 3
        if (dto.weight() != null && dto.weight() > 500 && dto.category() != 'H') {
            context.buildConstraintViolationWithTemplate("Heavy products must belong to category H")
                    .addPropertyNode("category")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
