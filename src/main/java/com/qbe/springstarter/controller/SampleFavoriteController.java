package com.qbe.springstarter.controller;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import com.qbe.springstarter.service.ISampleFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SampleFavoriteController implements ISampleFavoriteController {

    private final ISampleFavoriteService sampleFavoriteService;

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.ADD_FAVORITE)
    public ResponseEntity<Void> addFavorite(Long id) {
        sampleFavoriteService.addFavorite(id);
        log.info("Adding SampleEntity to favorites id={}", id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.REMOVE_FAVORITE)
    public ResponseEntity<Void> removeFavorite(Long id) {
        sampleFavoriteService.removeFavorite(id);
        log.info("Removing SampleEntity from favorites id={}", id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
