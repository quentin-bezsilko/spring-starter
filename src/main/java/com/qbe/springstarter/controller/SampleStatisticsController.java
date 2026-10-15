package com.qbe.springstarter.controller;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.dto.statistics.SampleStatisticsDto;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import com.qbe.springstarter.service.ISampleStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
public class SampleStatisticsController implements ISampleStatisticsController {

    private final ISampleStatisticsService sampleStatisticsService;

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.STATISTICS)
    public ResponseEntity<SampleStatisticsDto> getStatistics() {
        return ResponseEntity.ok(sampleStatisticsService.getStatistics());
    }
}
