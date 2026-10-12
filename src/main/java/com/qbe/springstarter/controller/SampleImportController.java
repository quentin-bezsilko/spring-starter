package com.qbe.springstarter.controller;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.dto.ImportResultDto;
import com.qbe.springstarter.error.TechnicalException;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import com.qbe.springstarter.service.ISampleImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
@Slf4j
public class SampleImportController implements ISampleImportController {

    private final ISampleImportService sampleImportService;

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.IMPORT)
    public ResponseEntity<ImportResultDto> importSamples(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new TechnicalException("The uploaded file is empty");
        }
        log.info("Importing samples from file={}, size={} bytes", file.getOriginalFilename(), file.getSize());
        return ResponseEntity.ok(sampleImportService.importSamples(file));
    }
}
