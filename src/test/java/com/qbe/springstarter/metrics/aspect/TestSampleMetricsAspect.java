package com.qbe.springstarter.metrics.aspect;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.error.VersionConflictException;
import com.qbe.springstarter.metrics.SampleMetrics;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestSampleMetricsAspect {

    private static final String OPERATION = "sample_create";

    @Mock
    private SampleMetrics sampleMetrics;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private SampleMetricAnnotation sampleMetricAnnotation;

    @Mock
    private Timer.Sample timer;

    private SampleMetricsAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new SampleMetricsAspect(sampleMetrics);

        when(sampleMetricAnnotation.operation()).thenReturn(OPERATION);
        when(sampleMetrics.startTimer()).thenReturn(timer);
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("Doit incrémenter le compteur de succès et arrêter le timer")
        void shouldRecordSuccess() throws Throwable {
            Object expectedResult = new Object();

            when(joinPoint.proceed()).thenReturn(expectedResult);

            Object result = aspect.recordMetrics(joinPoint, sampleMetricAnnotation);

            assertSame(expectedResult, result);

            verify(sampleMetrics).startTimer();
            verify(joinPoint).proceed();
            verify(sampleMetrics).increment(OPERATION, SampleMetrics.SUCCESS);
            verify(sampleMetrics).stopTimer(timer, OPERATION, SampleMetrics.SUCCESS);

            verify(sampleMetrics, never()).incrementTechnicalError();
        }
    }

    @Nested
    @DisplayName("NotFoundException")
    class NotFound {

        @Test
        @DisplayName("Doit incrémenter le compteur not_found et propager l'exception")
        void shouldRecordNotFound() throws Throwable {
            NotFoundException exception = new NotFoundException("SampleEntity", 1L);

            when(joinPoint.proceed()).thenThrow(exception);

            NotFoundException thrown = assertThrows(
                    NotFoundException.class, () -> aspect.recordMetrics(joinPoint, sampleMetricAnnotation));

            assertSame(exception, thrown);

            verify(sampleMetrics).increment(OPERATION, SampleMetrics.NOT_FOUND);
            verify(sampleMetrics).stopTimer(timer, OPERATION, SampleMetrics.NOT_FOUND);

            verify(sampleMetrics, never()).incrementTechnicalError();
            verify(sampleMetrics, never()).increment(OPERATION, SampleMetrics.SUCCESS);
            verify(sampleMetrics, never()).increment(OPERATION, SampleMetrics.ERROR);
        }
    }

    @Nested
    @DisplayName("VersionConflictException")
    class Conflict {

        @Test
        @DisplayName("Doit incrémenter le compteur conflict et propager l'exception")
        void shouldRecordConflict() throws Throwable {
            VersionConflictException exception = new VersionConflictException(1L, 1L, 2L);

            when(joinPoint.proceed()).thenThrow(exception);

            VersionConflictException thrown = assertThrows(
                    VersionConflictException.class, () -> aspect.recordMetrics(joinPoint, sampleMetricAnnotation));

            assertSame(exception, thrown);

            verify(sampleMetrics).increment(OPERATION, SampleMetrics.CONFLICT);
            verify(sampleMetrics).stopTimer(timer, OPERATION, SampleMetrics.CONFLICT);

            verify(sampleMetrics, never()).incrementTechnicalError();
            verify(sampleMetrics, never()).increment(OPERATION, SampleMetrics.SUCCESS);
            verify(sampleMetrics, never()).increment(OPERATION, SampleMetrics.ERROR);
        }
    }

    @Nested
    @DisplayName("Technical error")
    class TechnicalError {

        @Test
        @DisplayName("Doit incrémenter les erreurs techniques et propager l'exception")
        void shouldRecordTechnicalError() throws Throwable {
            RuntimeException exception = new RuntimeException("Database error");

            when(joinPoint.proceed()).thenThrow(exception);

            RuntimeException thrown =
                    assertThrows(RuntimeException.class, () -> aspect.recordMetrics(joinPoint, sampleMetricAnnotation));

            assertSame(exception, thrown);

            verify(sampleMetrics).increment(OPERATION, SampleMetrics.ERROR);
            verify(sampleMetrics).incrementTechnicalError();
            verify(sampleMetrics).stopTimer(timer, OPERATION, SampleMetrics.ERROR);

            verify(sampleMetrics, never()).increment(OPERATION, SampleMetrics.SUCCESS);
        }
    }
}
