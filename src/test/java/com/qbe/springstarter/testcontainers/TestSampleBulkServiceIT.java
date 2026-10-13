package com.qbe.springstarter.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import com.qbe.springstarter.dto.BulkResultDto;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.repository.SampleRepository;
import com.qbe.springstarter.service.SampleBulkService;
import com.qbe.springstarter.service.SampleService;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TestSampleBulkServiceIT extends TestAbstractIntegration {

    @Autowired
    private SampleBulkService sampleBulkService;

    @Autowired
    private SampleService sampleService;

    @Autowired
    private SampleRepository sampleRepository;

    @Test
    void shouldBulkDeleteSamples() {
        SampleDto first = sampleService.create(buildSampleDto("BULK-DELETE-1-" + UUID.randomUUID()));
        SampleDto second = sampleService.create(buildSampleDto("BULK-DELETE-2-" + UUID.randomUUID()));
        BulkResultDto result = sampleBulkService.bulkDelete(List.of(first.id(), second.id()));

        assertThat(result.success()).isEqualTo(2);
        assertThat(result.failed()).isZero();
        assertThat(sampleRepository.existsById(first.id())).isFalse();
        assertThat(sampleRepository.existsById(second.id())).isFalse();
    }

    @Test
    void shouldBulkDuplicateSamples() {
        SampleDto first = sampleService.create(buildSampleDto("BULK-DUPLICATE-1-" + UUID.randomUUID()));
        SampleDto second = sampleService.create(buildSampleDto("BULK-DUPLICATE-2-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        long before = sampleRepository.count();
        BulkResultDto result = sampleBulkService.bulkDuplicate(List.of(first.id(), second.id()));
        long after = sampleRepository.count();

        assertThat(result.success()).isEqualTo(2);
        assertThat(result.failed()).isZero();
        assertThat(after).isEqualTo(before + 2);
    }

    @Test
    void shouldBulkUpdateStatus() {
        SampleDto first = sampleService.create(buildSampleDto("BULK-STATUS-1-" + UUID.randomUUID()).toBuilder()
                .status(Status.CREATED)
                .build());
        SampleDto second = sampleService.create(buildSampleDto("BULK-STATUS-2-" + UUID.randomUUID()).toBuilder()
                .status(Status.CREATED)
                .externalId(UUID.randomUUID())
                .build());
        BulkResultDto result = sampleBulkService.bulkUpdateStatus(List.of(first.id(), second.id()), Status.ARCHIVED);

        assertThat(result.success()).isEqualTo(2);
        assertThat(result.failed()).isZero();
        assertThat(sampleService.findById(first.id()).status()).isEqualTo(Status.ARCHIVED);
        assertThat(sampleService.findById(second.id()).status()).isEqualTo(Status.ARCHIVED);
    }

    @Test
    void shouldBulkExportSamples() throws Exception {
        SampleDto first = sampleService.create(buildSampleDto("BULK-EXPORT-1-" + UUID.randomUUID()));
        SampleDto second = sampleService.create(buildSampleDto("BULK-EXPORT-2-" + UUID.randomUUID()).toBuilder()
                .externalId(UUID.randomUUID())
                .build());

        byte[] result = sampleBulkService.bulkExport(List.of(first.id(), second.id()));
        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void shouldGenerateValidWorkbookForBulkExport() throws Exception {
        SampleDto sample = sampleService.create(buildSampleDto("BULK-WORKBOOK-" + UUID.randomUUID()));
        byte[] result = sampleBulkService.bulkExport(List.of(sample.id()));
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("Samples");
            assertThat(workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue())
                    .isEqualTo("Id");
            assertThat(workbook.getSheetAt(0).getRow(0).getCell(1).getStringCellValue())
                    .isEqualTo("Nom");
        }
    }

    @Test
    void shouldExportEmptyWorkbook() throws Exception {
        byte[] result = sampleBulkService.bulkExport(List.of());
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(workbook.getSheetAt(0).getLastRowNum()).isZero();
        }
    }
}
