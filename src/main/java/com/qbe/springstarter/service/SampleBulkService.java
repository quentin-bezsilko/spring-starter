package com.qbe.springstarter.service;

import com.qbe.springstarter.constants.SpringStarterConstants;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.dto.bulk.BulkResultDto;
import com.qbe.springstarter.entity.SampleEntity;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.repository.SampleRepository;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SampleBulkService implements ISampleBulkService {

    private final SampleService sampleService;
    private final SampleRepository sampleRepository;

    @Override
    @Transactional
    public BulkResultDto bulkDelete(List<Long> ids) {
        int success = 0;
        List<String> errors = new ArrayList<>();
        for (Long id : ids) {
            try {
                sampleService.delete(id);
                success++;
            } catch (Exception e) {
                log.error("Failed to delete sample {}", id, e);
                errors.add("Sample " + id + " : " + e.getMessage());
            }
        }
        return new BulkResultDto(success, errors.size(), errors);
    }

    @Override
    @Transactional
    public BulkResultDto bulkDuplicate(List<Long> ids) {
        int success = 0;
        List<String> errors = new ArrayList<>();
        for (Long id : ids) {
            try {
                sampleService.duplicate(id);
                success++;
            } catch (Exception e) {
                log.error("Failed to duplicate sample {}", id, e);
                errors.add("Sample " + id + " : " + e.getMessage());
            }
        }
        return new BulkResultDto(success, errors.size(), errors);
    }

    @Override
    @Transactional
    public BulkResultDto bulkUpdateStatus(List<Long> ids, Status status) {
        int success = 0;
        List<String> errors = new ArrayList<>();
        for (Long id : ids) {
            try {
                SampleDto sample = sampleService.findById(id);
                SampleDto updated = sample.toBuilder().status(status).build();
                sampleService.update(id, updated);
                success++;
            } catch (Exception e) {
                log.error("Failed to update status for sample {}", id, e);
                errors.add("Sample " + id + " : " + e.getMessage());
            }
        }
        return new BulkResultDto(success, errors.size(), errors);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] bulkExport(List<Long> ids) {
        List<SampleEntity> samples = sampleRepository.findAllById(ids);
        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Samples");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < SpringStarterConstants.HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(SpringStarterConstants.HEADERS[i]);
            }
            int rowIndex = 1;
            for (SampleEntity sample : samples) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(sample.getId());
                row.createCell(1).setCellValue(sample.getName());
                row.createCell(2).setCellValue(sample.getDescription());
                row.createCell(3)
                        .setCellValue(
                                sample.getStatus() != null ? sample.getStatus().name() : "");
                row.createCell(4)
                        .setCellValue(
                                sample.getCategory() != null
                                        ? sample.getCategory().toString()
                                        : "");
                row.createCell(5).setCellValue(sample.getQuantity() != null ? sample.getQuantity() : 0);
                row.createCell(6).setCellValue(sample.getStock() != null ? sample.getStock() : 0);
                row.createCell(7).setCellValue(sample.getWeight() != null ? sample.getWeight() : 0d);
                row.createCell(8).setCellValue(sample.getRatio() != null ? sample.getRatio() : 0f);
                row.createCell(9)
                        .setCellValue(
                                sample.getPrice() != null ? sample.getPrice().doubleValue() : 0d);

                row.createCell(10).setCellValue(Boolean.TRUE.equals(sample.getActive()));

                row.createCell(11)
                        .setCellValue(
                                sample.getManufacturedDate() != null
                                        ? sample.getManufacturedDate().toString()
                                        : "");
                row.createCell(12)
                        .setCellValue(
                                sample.getManufacturedTime() != null
                                        ? sample.getManufacturedTime().toString()
                                        : "");
                row.createCell(13).setCellValue(sample.getComments() != null ? sample.getComments() : "");
                row.createCell(14)
                        .setCellValue(
                                sample.getExternalId() != null
                                        ? sample.getExternalId().toString()
                                        : "");

                row.createCell(15)
                        .setCellValue(
                                sample.getCreatedAt() != null
                                        ? sample.getCreatedAt().toString()
                                        : "");
                row.createCell(16)
                        .setCellValue(
                                sample.getUpdatedAt() != null
                                        ? sample.getUpdatedAt().toString()
                                        : "");
                row.createCell(17).setCellValue(sample.getVersion() != null ? sample.getVersion() : 0);
            }
            for (int i = 0; i < SpringStarterConstants.HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
            log.info("Bulk export completed: {} samples", samples.size());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error during bulk export", e);
            throw new RuntimeException("Failed to export selected samples", e);
        }
    }
}
