package com.qbe.springstarter.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class TestGlobalExceptionHandler {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        exceptionHandler = new GlobalExceptionHandler();

        when(httpServletRequest.getRequestURI()).thenReturn("/api/v1/samples/1");
    }

    @Test
    void shouldHandleNotFoundException() {
        NotFoundException exception = new NotFoundException("sample", 1L);
        ProblemDetail problemDetail = exceptionHandler.handleNotFoundException(exception, httpServletRequest);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Resource not found");
        assertThat(problemDetail.getDetail()).contains("sample");
        assertThat(problemDetail.getProperties()).containsEntry("path", "/api/v1/samples/1");
    }

    @Test
    void shouldHandleBusinessException() {
        BusinessException exception = new BusinessException("BUSINESS_ERROR", "Business error");
        ProblemDetail problemDetail = exceptionHandler.handleBusinessException(exception, httpServletRequest);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Business error");
        assertThat(problemDetail.getDetail()).isEqualTo("Business error");
        assertThat(problemDetail.getProperties()).containsEntry("code", "BUSINESS_ERROR");
        assertThat(problemDetail.getProperties()).containsEntry("path", "/api/v1/samples/1");
    }

    @Test
    void shouldHandleTechnicalException() {
        TechnicalException exception = new TechnicalException("Technical failure");
        ProblemDetail problemDetail = exceptionHandler.handleTechnicalException(exception, httpServletRequest);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Technical error");
        assertThat(problemDetail.getDetail()).isEqualTo("Technical failure");
        assertThat(problemDetail.getProperties()).containsEntry("path", "/api/v1/samples/1");
    }

    @Test
    void shouldHandleValidationException() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "sampleDto");

        bindingResult.addError(new FieldError("sampleDto", "name", "Name is mandatory"));
        bindingResult.addError(new FieldError("sampleDto", "price", "Price is mandatory"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        ProblemDetail problemDetail = exceptionHandler.handleValidationException(exception, httpServletRequest);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Validation failed");
        assertThat(problemDetail.getDetail()).isEqualTo("One or more validation errors occurred.");
        assertThat(problemDetail.getProperties()).containsEntry("path", "/api/v1/samples/1");

        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) problemDetail.getProperties().get("errors");

        assertThat(errors).containsEntry("name", "Name is mandatory").containsEntry("price", "Price is mandatory");
    }

    @Test
    void shouldHandleValidationExceptionWithoutFieldErrors() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "sampleDto");

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException((MethodParameter) null, bindingResult);

        ProblemDetail problemDetail = exceptionHandler.handleValidationException(exception, httpServletRequest);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Validation failed");
        assertThat(problemDetail.getDetail()).isEqualTo("One or more validation errors occurred.");
        assertThat(problemDetail.getProperties()).containsEntry("path", "/api/v1/samples/1");

        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) problemDetail.getProperties().get("errors");

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldHandleUnhandledException() {
        Exception exception = new RuntimeException("Unexpected error");
        ProblemDetail problemDetail = exceptionHandler.handleUnhandledException(exception, httpServletRequest);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Unexpected error");
        assertThat(problemDetail.getDetail()).isEqualTo("An unexpected error occurred.");
        assertThat(problemDetail.getProperties()).containsEntry("path", "/api/v1/samples/1");
    }

    @Test
    void shouldHandleUserNotFoundException() {
        UserNotFoundException exception = new UserNotFoundException(1L);
        ProblemDetail problemDetail = exceptionHandler.handleUserNotFound(exception, httpServletRequest);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problemDetail.getTitle()).isEqualTo("User not found");
        assertThat(problemDetail.getDetail()).isEqualTo(exception.getMessage());
        assertThat(problemDetail.getProperties()).containsEntry("path", "/api/v1/samples/1");
    }

    @Test
    void shouldHandleVersionConflictException() {
        VersionConflictException exception = new VersionConflictException(1L, 1L, 2L);
        ProblemDetail problemDetail = exceptionHandler.handleVersionConflict(exception, httpServletRequest);

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Version conflict");
        assertThat(problemDetail.getDetail()).isEqualTo(exception.getMessage());
        assertThat(problemDetail.getProperties()).containsEntry("path", "/api/v1/samples/1");
    }

    @Test
    void shouldHandleOptimisticLockingFailureException() {
        ObjectOptimisticLockingFailureException exception =
                new ObjectOptimisticLockingFailureException("SampleEntity", 1L);

        ResponseEntity<ProblemDetail> response = exceptionHandler.handleOptimisticLock(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();

        ProblemDetail problemDetail = response.getBody();

        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problemDetail.getTitle()).isEqualTo("Version conflict");
        assertThat(problemDetail.getDetail()).isEqualTo("The resource has been modified by another user");
    }
}
