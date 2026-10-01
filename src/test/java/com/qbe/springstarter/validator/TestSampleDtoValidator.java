package com.qbe.springstarter.validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.enums.Status;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestSampleDtoValidator {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    private SampleDtoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SampleDtoValidator();
    }

    @Test
    @DisplayName("Doit accepter un DTO null")
    void shouldAcceptNullDto() {
        assertTrue(validator.isValid(null, context));

        verifyNoInteractions(context);
    }

    @Nested
    @DisplayName("Produit actif")
    class ActiveProduct {

        @Test
        @DisplayName("Doit refuser un produit actif sans stock")
        void shouldRejectActiveProductWithNullStock() {
            SampleDto dto = validSample().active(true).stock(null).build();

            mockViolation("An active product must have stock", "stock");

            assertFalse(validator.isValid(dto, context));

            verify(context).disableDefaultConstraintViolation();
            verifyViolation("An active product must have stock", "stock");
        }

        @Test
        @DisplayName("Doit refuser un produit actif avec un stock à zéro")
        void shouldRejectActiveProductWithZeroStock() {
            SampleDto dto = validSample().active(true).stock(0L).build();

            mockViolation("An active product must have stock", "stock");

            assertFalse(validator.isValid(dto, context));

            verifyViolation("An active product must have stock", "stock");
        }

        @Test
        @DisplayName("Doit refuser un produit actif avec un stock négatif")
        void shouldRejectActiveProductWithNegativeStock() {
            SampleDto dto = validSample().active(true).stock(-1L).build();

            mockViolation("An active product must have stock", "stock");

            assertFalse(validator.isValid(dto, context));

            verifyViolation("An active product must have stock", "stock");
        }

        @Test
        @DisplayName("Doit accepter un produit actif avec du stock")
        void shouldAcceptActiveProductWithStock() {
            SampleDto dto = validSample().active(true).stock(10L).build();

            assertTrue(validator.isValid(dto, context));

            verify(context).disableDefaultConstraintViolation();
            verifyNoMoreInteractions(context);
        }
    }

    @Nested
    @DisplayName("Produit supprimé")
    class DeletedProduct {

        @Test
        @DisplayName("Doit refuser un produit supprimé avec du stock")
        void shouldRejectDeletedProductWithStock() {
            SampleDto dto = validSample()
                    .active(false)
                    .status(Status.DELETED)
                    .stock(10L)
                    .build();

            mockViolation("A deleted product cannot have stock", "stock");

            assertFalse(validator.isValid(dto, context));

            verify(context).disableDefaultConstraintViolation();
            verifyViolation("A deleted product cannot have stock", "stock");
        }

        @Test
        @DisplayName("Doit accepter un produit supprimé sans stock")
        void shouldAcceptDeletedProductWithNullStock() {
            SampleDto dto = validSample()
                    .active(false)
                    .status(Status.DELETED)
                    .stock(null)
                    .build();

            assertTrue(validator.isValid(dto, context));

            verify(context).disableDefaultConstraintViolation();
            verifyNoMoreInteractions(context);
        }

        @Test
        @DisplayName("Doit accepter un produit supprimé avec un stock à zéro")
        void shouldAcceptDeletedProductWithZeroStock() {
            SampleDto dto =
                    validSample().active(false).status(Status.DELETED).stock(0L).build();

            assertTrue(validator.isValid(dto, context));

            verify(context).disableDefaultConstraintViolation();
            verifyNoMoreInteractions(context);
        }
    }

    @Nested
    @DisplayName("Produit lourd")
    class HeavyProduct {

        @Test
        @DisplayName("Doit refuser un produit lourd hors catégorie H")
        void shouldRejectHeavyProductOutsideCategoryH() {
            SampleDto dto = validSample().weight(501.0).category('A').build();

            mockViolation("Heavy products must belong to category H", "category");

            assertFalse(validator.isValid(dto, context));

            verify(context).disableDefaultConstraintViolation();
            verifyViolation("Heavy products must belong to category H", "category");
        }

        @Test
        @DisplayName("Doit accepter un produit lourd en catégorie H")
        void shouldAcceptHeavyProductInCategoryH() {
            SampleDto dto = validSample().weight(501.0).category('H').build();

            assertTrue(validator.isValid(dto, context));

            verify(context).disableDefaultConstraintViolation();
            verifyNoMoreInteractions(context);
        }

        @Test
        @DisplayName("Doit accepter un produit de poids égal à 500 hors catégorie H")
        void shouldAcceptProductAtWeightLimitOutsideCategoryH() {
            SampleDto dto = validSample().weight(500.0).category('A').build();

            assertTrue(validator.isValid(dto, context));

            verify(context).disableDefaultConstraintViolation();
            verifyNoMoreInteractions(context);
        }

        @Test
        @DisplayName("Doit refuser un produit lourd sans catégorie")
        void shouldRejectHeavyProductWithoutCategory() {
            SampleDto dto = validSample().weight(501.0).category(null).build();

            mockViolation("Heavy products must belong to category H", "category");

            assertFalse(validator.isValid(dto, context));

            verifyViolation("Heavy products must belong to category H", "category");
        }
    }

    @Test
    @DisplayName("Doit accepter un produit respectant toutes les règles")
    void shouldAcceptValidSample() {
        SampleDto dto = validSample().build();

        assertTrue(validator.isValid(dto, context));

        verify(context).disableDefaultConstraintViolation();
        verifyNoMoreInteractions(context);
    }

    private SampleDto.SampleDtoBuilder validSample() {
        return SampleDto.builder()
                .active(true)
                .stock(10L)
                .status(Status.ACTIVE)
                .weight(100.0)
                .category('A');
    }

    private void mockViolation(String message, String property) {
        when(context.buildConstraintViolationWithTemplate(message)).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(property)).thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation()).thenReturn(context);
    }

    private void verifyViolation(String message, String property) {
        verify(context).buildConstraintViolationWithTemplate(message);
        verify(violationBuilder).addPropertyNode(property);
        verify(nodeBuilder).addConstraintViolation();
    }
}
