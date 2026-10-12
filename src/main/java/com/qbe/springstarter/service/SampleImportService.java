package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.ImportErrorDto;
import com.qbe.springstarter.dto.ImportResultDto;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.enums.Status;
import com.qbe.springstarter.error.TechnicalException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class SampleImportService implements ISampleImportService {

    private final SampleService sampleService;

    @Override
    public ImportResultDto importSamples(MultipartFile file) {
        List<ImportErrorDto> errors = new ArrayList<>();
        int created = 0;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }
                try {
                    SampleDto dto = buildDto(row);
                    sampleService.create(dto);
                    created++;
                } catch (Exception e) {
                    errors.add(new ImportErrorDto(rowIndex + 1, e.getMessage()));
                }
            }
            return new ImportResultDto(created, errors.size(), errors);
        } catch (IOException e) {
            log.error("Failed to import samples", e);
            throw new TechnicalException("Failed to import samples");
        }
    }

    private SampleDto buildDto(Row row) {
        return SampleDto.builder()
                .name(getRequiredString(row, 0, "Name"))
                .description(getString(row, 1))
                .status(getRequiredStatus(row, 2))
                .category(getRequiredCategory(row, 3))
                .quantity(getRequiredInteger(row, 4, "Quantity"))
                .stock(getNullableLong(row, 5))
                .weight(getNullableDouble(row, 6))
                .ratio(getNullableFloat(row, 7))
                .price(getRequiredBigDecimal(row, 8, "Price"))
                .active(getRequiredBoolean(row, 9, "Active"))
                .manufacturedDate(parseLocalDate(getString(row, 10)))
                .manufacturedTime(parseLocalTime(getString(row, 11)))
                .comments(getString(row, 12))
                .externalId(UUID.randomUUID())
                .version(0L)
                .build();
    }

    private String getString(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? "" : cell.toString().trim();
    }

    private double getNumeric(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return 0d;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            return Double.parseDouble(cell.getStringCellValue());
        }
        return 0d;
    }

    private Double getNullableDouble(Row row, int index) {
        String value = getString(row, index);
        return value.isBlank() ? null : getNumeric(row, index);
    }

    private Float getNullableFloat(Row row, int index) {
        String value = getString(row, index);
        return value.isBlank() ? null : (float) getNumeric(row, index);
    }

    private LocalDate parseLocalDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private LocalTime parseLocalTime(String value) {
        return value == null || value.isBlank() ? null : LocalTime.parse(value);
    }

    private String getRequiredString(Row row, int index, String fieldName) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is mandatory");
        }
        return value;
    }

    private Integer getRequiredInteger(Row row, int index, String fieldName) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is mandatory");
        }
        return (int) getNumeric(row, index);
    }

    private BigDecimal getRequiredBigDecimal(Row row, int index, String fieldName) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is mandatory");
        }
        return BigDecimal.valueOf(getNumeric(row, index));
    }

    private Boolean getRequiredBoolean(Row row, int index, String fieldName) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is mandatory");
        }
        if ("true".equalsIgnoreCase(value) || "oui".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value) || "non".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException(fieldName + " must be true/false");
    }

    private Status getRequiredStatus(Row row, int index) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Status is mandatory");
        }
        try {
            return Status.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + value);
        }
    }

    private Character getRequiredCategory(Row row, int index) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Category is mandatory");
        }
        return value.charAt(0);
    }

    private Long getNullableLong(Row row, int index) {
        String value = getString(row, index);
        if (value.isBlank()) {
            return null;
        }
        return (long) getNumeric(row, index);
    }
}
