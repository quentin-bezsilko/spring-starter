package com.qbe.springstarter.service;

import com.qbe.springstarter.dto.ImportErrorDto;
import com.qbe.springstarter.dto.ImportResultDto;
import com.qbe.springstarter.dto.SampleDto;
import com.qbe.springstarter.error.TechnicalException;
import com.qbe.springstarter.utils.ImportUtils;
import java.io.IOException;
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
                .name(ImportUtils.getRequiredString(row, 0, "Name"))
                .description(ImportUtils.getString(row, 1))
                .status(ImportUtils.getRequiredStatus(row, 2))
                .category(ImportUtils.getRequiredCategory(row, 3))
                .quantity(ImportUtils.getRequiredInteger(row, 4, "Quantity"))
                .stock(ImportUtils.getNullableLong(row, 5))
                .weight(ImportUtils.getNullableDouble(row, 6))
                .ratio(ImportUtils.getNullableFloat(row, 7))
                .price(ImportUtils.getRequiredBigDecimal(row, 8, "Price"))
                .active(ImportUtils.getRequiredBoolean(row, 9, "Active"))
                .manufacturedDate(ImportUtils.parseLocalDate(ImportUtils.getString(row, 10)))
                .manufacturedTime(ImportUtils.parseLocalTime(ImportUtils.getString(row, 11)))
                .comments(ImportUtils.getString(row, 12))
                .externalId(UUID.randomUUID())
                .version(0L)
                .build();
    }
}
