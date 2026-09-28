package com.qbe.springstarter.controller;

import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.service.SampleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<SampleDto> create(@Valid @RequestBody SampleDto dto) {
        log.info("Creating SampleEntity with data: {}", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @Override
    public ResponseEntity<SampleDto> findById(Long id) {
        log.info("Finding SampleEntity by id={}", id);
        return ResponseEntity.status(HttpStatus.OK).body(service.findById(id));
    }

    @Override
    public ResponseEntity<List<SampleDto>> findAll() {
        log.info("Finding all SampleEntities");
        return ResponseEntity.status(HttpStatus.OK).body(service.findAll());
    }

    @Override
    public ResponseEntity<SampleDto> update(Long id, @Valid @RequestBody SampleDto dto) {
        log.info("Updating SampleEntity id={}", id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.update(id, dto));
    }

    @Override
    public ResponseEntity<Void> delete(Long id) {
        log.info("Deleting SampleEntity id={}", id);
        service.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
