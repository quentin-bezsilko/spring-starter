package com.qbe.springstarter.controller;

import com.qbe.springstarter.constants.MetricsConstants;
import com.qbe.springstarter.dto.ImportResultDto;
import com.qbe.springstarter.error.EmptyImportFileException;
import com.qbe.springstarter.error.InvalidImportFileException;
import com.qbe.springstarter.metrics.annotation.SampleMetricAnnotation;
import com.qbe.springstarter.service.ISampleImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SampleImportController implements ISampleImportController {

    private final ISampleImportService sampleImportService;

    @Override
    @SampleMetricAnnotation(operation = MetricsConstants.IMPORT)
    public ResponseEntity<ImportResultDto> importSamples(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new EmptyImportFileException("The uploaded file is empty");
        }

        String fileName = file.getOriginalFilename();
        boolean excelFile = fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"));
        if (!excelFile) {
            throw new InvalidImportFileException("Only Excel files (.xlsx, .xls) are supported");
        }

        log.info("Importing samples from file={}, size={} bytes", fileName, file.getSize());
        return ResponseEntity.ok(sampleImportService.importSamples(file));
    }
}
