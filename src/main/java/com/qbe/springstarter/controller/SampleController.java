package com.qbe.springstarter.controller;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.dto.*;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import com.qbe.springstarter.service.ISampleBulkService;
import com.qbe.springstarter.service.ISampleService;
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

    private final ISampleService sampleService;
    private final ISampleBulkService sampleBulkService;

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.CREATE)
    public ResponseEntity<SampleDto> create(@Valid @RequestBody SampleDto dto) {
        log.info("Creating SampleEntity with data: {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(sampleService.create(dto));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.FIND_BY_ID)
    public ResponseEntity<SampleDto> findById(Long id) {
        log.info("Finding SampleEntity by id={}", id);
        return ResponseEntity.status(HttpStatus.OK).body(sampleService.findById(id));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.FIND_ALL)
    public ResponseEntity<Page<SampleDto>> findAll(
            String search,
            Status status,
            Boolean includeDeleted,
            Character category,
            Boolean active,
            Pageable pageable) {

        log.info(
                "Finding SampleEntities search={}, status={}, category={}, active={}, page={}, size={}, sort={}, includeDeleted={}",
                search,
                status,
                category,
                active,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort(),
                includeDeleted);

        return ResponseEntity.ok(sampleService.findAll(search, status, includeDeleted, category, active, pageable));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.UPDATE)
    public ResponseEntity<SampleDto> update(Long id, @Valid @RequestBody SampleDto dto) {
        log.info("Updating SampleEntity id={}", id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(sampleService.update(id, dto));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.DELETE)
    public ResponseEntity<Void> delete(Long id) {
        log.info("Deleting SampleEntity id={}", id);
        sampleService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.UPDATE_STATUS)
    public ResponseEntity<SampleDto> updateStatus(Long id, UpdateSampleStatusDto request) {
        log.info("Updating SampleEntity id={} status={}", id, request.status());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(sampleService.updateStatus(id, request.status()));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.DUPLICATE)
    public ResponseEntity<SampleDto> duplicate(Long id) {
        log.info("Duplicating SampleEntity id={}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(sampleService.duplicate(id));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.EXPORT)
    public ResponseEntity<byte[]> export(
            String search, Status status, Boolean includeDeleted, Character category, Boolean active) {
        log.info("Exporting samples search={}, status={}, category={}, active={}", search, status, category, active);
        String fileName = "samples-" + LocalDate.now() + ".xlsx";
        byte[] file = sampleService.export(search, status, includeDeleted, category, active);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.BULK_DELETE)
    public ResponseEntity<BulkResultDto> bulkDelete(BulkIdsDto request) {
        log.info("Deleting samples in bulk ids={}", request.ids());
        return ResponseEntity.ok(sampleBulkService.bulkDelete(request.ids()));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.BULK_DUPLICATE)
    public ResponseEntity<BulkResultDto> bulkDuplicate(BulkIdsDto request) {
        log.info("Duplicating samples in bulk ids={}", request.ids());
        return ResponseEntity.ok(sampleBulkService.bulkDuplicate(request.ids()));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.BULK_STATUS)
    public ResponseEntity<BulkResultDto> bulkUpdateStatus(@Valid BulkStatusDto request) {
        log.info("Updating samples status in bulk ids={} status={}", request.ids(), request.status());
        return ResponseEntity.ok(sampleBulkService.bulkUpdateStatus(request.ids(), request.status()));
    }

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.BULK_EXPORT)
    public ResponseEntity<byte[]> bulkExport(BulkIdsDto request) {
        log.info("Exporting samples in bulk ids={}", request.ids());
        String fileName = "samples-selection-" + LocalDate.now() + ".xlsx";
        byte[] file = sampleBulkService.bulkExport(request.ids());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
