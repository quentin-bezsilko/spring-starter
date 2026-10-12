package com.qbe.springstarter.controller;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import com.qbe.springstarter.service.SampleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public ResponseEntity<Page<SampleDto>> findAll(
            String search, Status status, Character category, Boolean active, Pageable pageable) {

        log.info(
                "Finding SampleEntities search={}, status={}, category={}, active={}, page={}, size={}, sort={}",
                search,
                status,
                category,
                active,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        return ResponseEntity.ok(service.findAll(search, status, category, active, pageable));
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

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.DUPLICATE)
    public ResponseEntity<SampleDto> duplicate(Long id) {
        log.info("Duplicating SampleEntity id={}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.duplicate(id));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.EXPORT)
    public ResponseEntity<byte[]> export(String search, Status status, Character category, Boolean active) {
        log.info("Exporting samples search={}, status={}, category={}, active={}", search, status, category, active);
        String fileName = "samples-" + LocalDate.now() + ".xlsx";
        byte[] file = service.export(search, status, category, active);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
