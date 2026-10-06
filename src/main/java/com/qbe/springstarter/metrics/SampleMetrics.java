package com.qbe.springstarter.metrics;

import com.qbe.springstarter.constants.MetricsConstants;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter.MeterProvider;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class SampleMetrics {

    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String NOT_FOUND = "not_found";
    public static final String CONFLICT = "conflict";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String FORBIDDEN = "forbidden";

    private final MeterRegistry registry;
    private final MeterProvider<Counter> operationCounter;
    private final MeterProvider<Timer> operationTimer;
    private final Counter technicalErrors;

    public SampleMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.operationCounter = Counter.builder(MetricsConstants.OPERATIONS_TOTAL)
                .description(MetricsConstants.OPERATIONS_TOTAL_DESCRIPTION)
                .withRegistry(registry);

        this.operationTimer = Timer.builder(MetricsConstants.OPERATION_DURATION)
                .description(MetricsConstants.OPERATION_DURATION_DESCRIPTION)
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .withRegistry(registry);

        this.technicalErrors = Counter.builder(MetricsConstants.TECHNICAL_ERRORS_TOTAL)
                .description(MetricsConstants.TECHNICAL_ERRORS_DESCRIPTION)
                .register(registry);
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample, String operation, String status) {
        sample.stop(operationTimer.withTags(
                MetricsConstants.OPERATION_TAG, operation, MetricsConstants.STATUS_TAG, status));
    }

    public void increment(String operation, String status) {
        operationCounter
                .withTags(MetricsConstants.OPERATION_TAG, operation, MetricsConstants.STATUS_TAG, status)
                .increment();
    }

    public void incrementTechnicalError() {
        technicalErrors.increment();
    }
}
