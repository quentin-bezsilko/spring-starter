package com.qbe.springstarter.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.qbe.springstarter.dto.ImportResultDto;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.error.TechnicalException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TestSampleImportService {

    @Mock
    private SampleService sampleService;

    @InjectMocks
    private SampleImportService sampleImportService;

    @Nested
    @DisplayName("importSamples")
    class ImportSamples {

        @Test
        @DisplayName("Doit importer une ligne valide")
        void shouldImportValidRow() throws Exception {
            MultipartFile file = buildExcelFile(true);

            when(sampleService.create(any(SampleDto.class))).thenReturn(mock(SampleDto.class));

            ImportResultDto result = sampleImportService.importSamples(file);
            assertEquals(1, result.created());
            assertEquals(0, result.failed());
            assertTrue(result.errors().isEmpty());

            verify(sampleService).create(any(SampleDto.class));
        }

        @Test
        @DisplayName("Doit ignorer une ligne invalide")
        void shouldCollectImportErrors() throws Exception {
            MultipartFile file = buildExcelFile(false);

            ImportResultDto result = sampleImportService.importSamples(file);
            assertEquals(0, result.created());
            assertEquals(1, result.failed());
            assertFalse(result.errors().isEmpty());

            verify(sampleService, never()).create(any());
        }

        @Test
        @DisplayName("Doit importer plusieurs lignes")
        void shouldImportMultipleRows() throws Exception {
            MultipartFile file = buildExcelFileWithTwoRows();

            when(sampleService.create(any(SampleDto.class))).thenReturn(mock(SampleDto.class));

            ImportResultDto result = sampleImportService.importSamples(file);
            assertEquals(2, result.created());
            assertEquals(0, result.failed());

            verify(sampleService, times(2)).create(any(SampleDto.class));
        }

        @Test
        @DisplayName("Doit retourner un résultat vide lorsque le fichier ne contient aucune ligne")
        void shouldHandleEmptyFile() throws Exception {
            MultipartFile file = buildEmptyExcelFile();

            ImportResultDto result = sampleImportService.importSamples(file);

            assertEquals(0, result.created());
            assertEquals(0, result.failed());

            verify(sampleService, never()).create(any());
        }

        @Test
        @DisplayName("Doit lever une TechnicalException si le fichier est illisible")
        void shouldThrowTechnicalException() throws Exception {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getInputStream()).thenThrow(new IOException("boom"));
            assertThrows(TechnicalException.class, () -> sampleImportService.importSamples(file));
        }

        @Test
        @DisplayName("Doit enregistrer une erreur lorsque le service échoue")
        void shouldHandleCreateException() throws Exception {
            MultipartFile file = buildExcelFile(true);
            when(sampleService.create(any(SampleDto.class))).thenThrow(new RuntimeException("creation error"));

            ImportResultDto result = sampleImportService.importSamples(file);
            assertEquals(0, result.created());
            assertEquals(1, result.failed());
            assertEquals(1, result.errors().size());
        }
    }

    private MultipartFile buildExcelFile(boolean valid) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Samples");
            Row header = sheet.createRow(0);
            for (int i = 0; i <= 17; i++) {
                header.createCell(i);
            }

            Row row = sheet.createRow(1);
            populateValidRow(row, valid ? "Sample-001" : "");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new MockMultipartFile(
                    "file",
                    "samples.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray());
        }
    }

    private MultipartFile buildExcelFileWithTwoRows() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Samples");
            sheet.createRow(0);

            for (int rowIndex = 1; rowIndex <= 2; rowIndex++) {
                Row row = sheet.createRow(rowIndex);
                populateValidRow(row, "Sample-" + rowIndex);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new MockMultipartFile(
                    "file",
                    "samples.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray());
        }
    }

    private MultipartFile buildEmptyExcelFile() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Samples");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new MockMultipartFile(
                    "file",
                    "samples.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray());
        }
    }

    private void populateValidRow(Row row, String name) {
        row.createCell(0).setCellValue(name);
        row.createCell(1).setCellValue("Description");
        row.createCell(2).setCellValue("ACTIVE");
        row.createCell(3).setCellValue("A");
        row.createCell(4).setCellValue(100);
        row.createCell(5).setCellValue(10);
        row.createCell(6).setCellValue(10.5);
        row.createCell(7).setCellValue(0.5);
        row.createCell(8).setCellValue(99.99);
        row.createCell(9).setCellValue("true");
        row.createCell(10).setCellValue("2024-01-01");
        row.createCell(11).setCellValue("12:00");
        row.createCell(12).setCellValue("comments");
    }
}
