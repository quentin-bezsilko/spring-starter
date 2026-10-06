package com.qbe.springstarter.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.qbe.springstarter.constants.MetricsConstants;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TestSampleMetrics {

    private static final String OPERATION = "sample_create";

    private SimpleMeterRegistry registry;
    private SampleMetrics sampleMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        sampleMetrics = new SampleMetrics(registry);
    }

    @Nested
    @DisplayName("increment")
    class Increment {

        @Test
        @DisplayName("Doit incrémenter le compteur de succès")
        void shouldIncrementSuccessCounter() {
            sampleMetrics.increment(OPERATION, SampleMetrics.SUCCESS);

            Counter counter = registry.find(MetricsConstants.OPERATIONS_TOTAL)
                    .tag(MetricsConstants.OPERATION_TAG, OPERATION)
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.SUCCESS)
                    .counter();

            assertNotNull(counter);
            assertEquals(1.0, counter.count());
        }

        @Test
        @DisplayName("Doit incrémenter le compteur d'erreur")
        void shouldIncrementErrorCounter() {
            sampleMetrics.increment(OPERATION, SampleMetrics.ERROR);

            Counter counter = registry.find(MetricsConstants.OPERATIONS_TOTAL)
                    .tag(MetricsConstants.OPERATION_TAG, OPERATION)
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.ERROR)
                    .counter();

            assertNotNull(counter);
            assertEquals(1.0, counter.count());
        }

        @Test
        @DisplayName("Doit incrémenter le compteur not_found")
        void shouldIncrementNotFoundCounter() {
            sampleMetrics.increment(OPERATION, SampleMetrics.NOT_FOUND);

            Counter counter = registry.find(MetricsConstants.OPERATIONS_TOTAL)
                    .tag(MetricsConstants.OPERATION_TAG, OPERATION)
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.NOT_FOUND)
                    .counter();

            assertNotNull(counter);
            assertEquals(1.0, counter.count());
        }

        @Test
        @DisplayName("Doit incrémenter le compteur conflict")
        void shouldIncrementConflictCounter() {
            sampleMetrics.increment(OPERATION, SampleMetrics.CONFLICT);

            Counter counter = registry.find(MetricsConstants.OPERATIONS_TOTAL)
                    .tag(MetricsConstants.OPERATION_TAG, OPERATION)
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.CONFLICT)
                    .counter();

            assertNotNull(counter);
            assertEquals(1.0, counter.count());
        }

        @Test
        @DisplayName("Doit cumuler plusieurs appels")
        void shouldIncrementCounterMultipleTimes() {
            sampleMetrics.increment(OPERATION, SampleMetrics.SUCCESS);
            sampleMetrics.increment(OPERATION, SampleMetrics.SUCCESS);
            sampleMetrics.increment(OPERATION, SampleMetrics.SUCCESS);

            Counter counter = registry.find(MetricsConstants.OPERATIONS_TOTAL)
                    .tag(MetricsConstants.OPERATION_TAG, OPERATION)
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.SUCCESS)
                    .counter();

            assertNotNull(counter);
            assertEquals(3.0, counter.count());
        }
    }

    @Nested
    @DisplayName("technicalErrors")
    class TechnicalErrors {

        @Test
        @DisplayName("Doit incrémenter le compteur d'erreurs techniques")
        void shouldIncrementTechnicalErrorCounter() {
            sampleMetrics.incrementTechnicalError();

            Counter counter =
                    registry.find(MetricsConstants.TECHNICAL_ERRORS_TOTAL).counter();

            assertNotNull(counter);
            assertEquals(1.0, counter.count());
        }

        @Test
        @DisplayName("Doit cumuler plusieurs erreurs techniques")
        void shouldIncrementTechnicalErrorCounterMultipleTimes() {
            sampleMetrics.incrementTechnicalError();
            sampleMetrics.incrementTechnicalError();

            Counter counter =
                    registry.find(MetricsConstants.TECHNICAL_ERRORS_TOTAL).counter();

            assertNotNull(counter);
            assertEquals(2.0, counter.count());
        }
    }

    @Nested
    @DisplayName("timer")
    class TimerMetrics {

        @Test
        @DisplayName("Doit démarrer et arrêter un timer")
        void shouldStartAndStopTimer() throws InterruptedException {
            Timer.Sample sample = sampleMetrics.startTimer();

            Thread.sleep(10);

            sampleMetrics.stopTimer(sample, OPERATION, SampleMetrics.SUCCESS);

            Timer timer = registry.find(MetricsConstants.OPERATION_DURATION)
                    .tag(MetricsConstants.OPERATION_TAG, OPERATION)
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.SUCCESS)
                    .timer();

            assertNotNull(timer);
            assertEquals(1L, timer.count());
        }

        @Test
        @DisplayName("Doit distinguer les timers par statut")
        void shouldSeparateTimersByStatus() {
            Timer.Sample successSample = sampleMetrics.startTimer();
            sampleMetrics.stopTimer(successSample, OPERATION, SampleMetrics.SUCCESS);

            Timer.Sample errorSample = sampleMetrics.startTimer();
            sampleMetrics.stopTimer(errorSample, OPERATION, SampleMetrics.ERROR);

            Timer successTimer = registry.find(MetricsConstants.OPERATION_DURATION)
                    .tag(MetricsConstants.OPERATION_TAG, OPERATION)
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.SUCCESS)
                    .timer();

            Timer errorTimer = registry.find(MetricsConstants.OPERATION_DURATION)
                    .tag(MetricsConstants.OPERATION_TAG, OPERATION)
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.ERROR)
                    .timer();

            assertNotNull(successTimer);
            assertNotNull(errorTimer);

            assertEquals(1L, successTimer.count());
            assertEquals(1L, errorTimer.count());
        }

        @Test
        @DisplayName("Doit enregistrer une durée")
        void shouldRecordDuration() {
            Timer.Sample sample = sampleMetrics.startTimer();

            Timer timer = Timer.builder(MetricsConstants.OPERATION_DURATION)
                    .tag(MetricsConstants.OPERATION_TAG, "manual_operation")
                    .tag(MetricsConstants.STATUS_TAG, SampleMetrics.SUCCESS)
                    .register(registry);

            timer.record(Duration.ofMillis(100));

            assertEquals(1L, timer.count());
            assertEquals(Duration.ofMillis(100).toNanos(), (long)
                    timer.totalTime(java.util.concurrent.TimeUnit.NANOSECONDS));
        }
    }
}
