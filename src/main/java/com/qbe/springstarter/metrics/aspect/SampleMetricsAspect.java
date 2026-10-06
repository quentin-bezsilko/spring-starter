package com.qbe.springstarter.metrics.aspect;

import com.qbe.springstarter.error.NotFoundException;
import com.qbe.springstarter.error.VersionConflictException;
import com.qbe.springstarter.metrics.SampleMetrics;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SampleMetricsAspect {

    private final SampleMetrics sampleMetrics;

    @Around("@annotation(sampleMetricAnnotation)")
    public Object recordMetrics(ProceedingJoinPoint joinPoint, SampleMetricAnnotation sampleMetricAnnotation)
            throws Throwable {
        String operation = sampleMetricAnnotation.operation();
        Timer.Sample timer = sampleMetrics.startTimer();
        String status = SampleMetrics.SUCCESS;
        try {
            Object result = joinPoint.proceed();
            sampleMetrics.increment(operation, status);
            return result;
        } catch (NotFoundException e) {
            status = SampleMetrics.NOT_FOUND;
            sampleMetrics.increment(operation, status);
            throw e;
        } catch (VersionConflictException e) {
            status = SampleMetrics.CONFLICT;
            sampleMetrics.increment(operation, status);
            throw e;
        } catch (Exception e) {
            status = SampleMetrics.ERROR;
            sampleMetrics.increment(operation, status);
            sampleMetrics.incrementTechnicalError();
            throw e;
        } finally {
            sampleMetrics.stopTimer(timer, operation, status);
        }
    }
}
