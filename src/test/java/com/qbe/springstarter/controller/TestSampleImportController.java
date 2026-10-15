package com.qbe.springstarter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.qbe.springstarter.dto.ImportResultDto;
import com.qbe.springstarter.error.EmptyImportFileException;
import com.qbe.springstarter.service.ISampleImportService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class TestSampleImportController {

    @Mock
    private ISampleImportService sampleImportService;

    @InjectMocks
    private SampleImportController sampleImportController;

    @Nested
    @DisplayName("importSamples")
    class ImportSamples {

        @Test
        @DisplayName("Doit retourner 200 avec le résultat de l'import")
        void shouldImportSamples() {
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "samples.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "content".getBytes());

            ImportResultDto result = new ImportResultDto(5, 0, List.of());

            when(sampleImportService.importSamples(file)).thenReturn(result);

            ResponseEntity<ImportResultDto> response = sampleImportController.importSamples(file);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(result, response.getBody());

            verify(sampleImportService).importSamples(file);
        }

        @Test
        @DisplayName("Doit lever une EmptyImportFileException lorsque le fichier est null")
        void shouldThrowEmptyImportFileExceptionWhenFileIsNull() {
            EmptyImportFileException exception =
                    assertThrows(EmptyImportFileException.class, () -> sampleImportController.importSamples(null));

            assertEquals("The uploaded file is empty", exception.getMessage());
        }

        @Test
        @DisplayName("Doit lever une EmptyImportFileException lorsque le fichier est vide")
        void shouldThrowEmptyImportFileExceptionWhenFileIsEmpty() {

            MultipartFile file = new MockMultipartFile(
                    "file",
                    "samples.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new byte[0]);

            EmptyImportFileException exception =
                    assertThrows(EmptyImportFileException.class, () -> sampleImportController.importSamples(file));

            assertEquals("The uploaded file is empty", exception.getMessage());
        }

        @Test
        @DisplayName("Doit transmettre le fichier au service")
        void shouldDelegateImportToService() {
            MultipartFile file = new MockMultipartFile(
                    "file",
                    "samples.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "content".getBytes());

            ImportResultDto result = new ImportResultDto(1, 0, List.of());

            when(sampleImportService.importSamples(file)).thenReturn(result);

            sampleImportController.importSamples(file);
            verify(sampleImportService).importSamples(file);
        }
    }
}
