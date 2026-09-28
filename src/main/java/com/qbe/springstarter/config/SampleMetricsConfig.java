package com.qbe.springstarter.config;

import com.qbe.springstarter.constants.MetricsConstants;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SampleMetricsConfig {

    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private static final String NOT_FOUND = "not_found";

    private final Counter createSuccess;
    private final Counter createError;

    private final Counter findSuccess;
    private final Counter findError;
    private final Counter findNotFound;

    private final Counter findAllSuccess;
    private final Counter findAllError;

    private final Counter updateSuccess;
    private final Counter updateError;
    private final Counter updateNotFound;

    private final Counter deleteSuccess;
    private final Counter deleteError;
    private final Counter deleteNotFound;

    private final Counter technicalErrors;

    private final Timer createTimer;
    private final Timer findByIdTimer;
    private final Timer findAllTimer;
    private final Timer updateTimer;
    private final Timer deleteTimer;

    public SampleMetricsConfig(MeterRegistry registry) {

        createSuccess = counter(registry, MetricsConstants.CREATE, SUCCESS);
        createError = counter(registry, MetricsConstants.CREATE, ERROR);

        findSuccess = counter(registry, MetricsConstants.FIND_BY_ID, SUCCESS);
        findError = counter(registry, MetricsConstants.FIND_BY_ID, ERROR);
        findNotFound = counter(registry, MetricsConstants.FIND_BY_ID, NOT_FOUND);

        findAllSuccess = counter(registry, MetricsConstants.FIND_ALL, SUCCESS);
        findAllError = counter(registry, MetricsConstants.FIND_ALL, ERROR);

        updateSuccess = counter(registry, MetricsConstants.UPDATE, SUCCESS);
        updateError = counter(registry, MetricsConstants.UPDATE, ERROR);
        updateNotFound = counter(registry, MetricsConstants.UPDATE, NOT_FOUND);

        deleteSuccess = counter(registry, MetricsConstants.DELETE, SUCCESS);
        deleteError = counter(registry, MetricsConstants.DELETE, ERROR);
        deleteNotFound = counter(registry, MetricsConstants.DELETE, NOT_FOUND);

        technicalErrors = Counter.builder(MetricsConstants.TECHNICAL_ERRORS_TOTAL)
                .description(MetricsConstants.TECHNICAL_ERRORS_DESCRIPTION)
                .register(registry);

        createTimer = timer(registry, MetricsConstants.CREATE);
        findByIdTimer = timer(registry, MetricsConstants.FIND_BY_ID);
        findAllTimer = timer(registry, MetricsConstants.FIND_ALL);
        updateTimer = timer(registry, MetricsConstants.UPDATE);
        deleteTimer = timer(registry, MetricsConstants.DELETE);
    }

    private Counter counter(MeterRegistry registry, String operation, String status) {
        return Counter.builder(MetricsConstants.OPERATIONS_TOTAL)
                .description(MetricsConstants.OPERATIONS_TOTAL_DESCRIPTION)
                .tag(MetricsConstants.OPERATION_TAG, operation)
                .tag(MetricsConstants.STATUS_TAG, status)
                .register(registry);
    }

    private Timer timer(MeterRegistry registry, String operation) {
        return Timer.builder(MetricsConstants.OPERATION_DURATION)
                .description(MetricsConstants.OPERATION_DURATION_DESCRIPTION)
                .tag(MetricsConstants.OPERATION_TAG, operation)
                .publishPercentileHistogram()
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}
