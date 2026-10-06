package com.qbe.springstarter.controller;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import com.qbe.springstarter.service.SampleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SampleController implements ISampleController {

    private final SampleService service;

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.CREATE)
    public ResponseEntity<SampleDto> create(@Valid @RequestBody SampleDto dto) {
        log.info("Creating SampleEntity with data: {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.FIND_BY_ID)
    public ResponseEntity<SampleDto> findById(Long id) {
        log.info("Finding SampleEntity by id={}", id);
        return ResponseEntity.status(HttpStatus.OK).body(service.findById(id));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.FIND_ALL)
    public ResponseEntity<Page<SampleDto>> findAll(Pageable pageable) {
        log.info(
                "Finding SampleEntities page={}, size={}, sort={}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());
        return ResponseEntity.status(HttpStatus.OK).body(service.findAll(pageable));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.UPDATE)
    public ResponseEntity<SampleDto> update(Long id, @Valid @RequestBody SampleDto dto) {
        log.info("Updating SampleEntity id={}", id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.update(id, dto));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.DELETE)
    public ResponseEntity<Void> delete(Long id) {
        log.info("Deleting SampleEntity id={}", id);
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
