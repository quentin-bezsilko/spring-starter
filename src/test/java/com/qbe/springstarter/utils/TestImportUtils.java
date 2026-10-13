package com.qbe.springstarter.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.qbe.springstarter.enums.Status;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TestImportUtils {

    @Nested
    @DisplayName("getString")
    class GetString {

        @Test
        void shouldReturnCellValue() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("test");

                assertEquals("test", ImportUtils.getString(row, 0));
            }
        }

        @Test
        void shouldReturnEmptyStringWhenCellIsNull() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);

                assertEquals("", ImportUtils.getString(row, 0));
            }
        }
    }

    @Nested
    @DisplayName("getNumeric")
    class GetNumeric {

        @Test
        void shouldReturnNumericValue() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue(12.5);

                assertEquals(12.5, ImportUtils.getNumeric(row, 0));
            }
        }

        @Test
        void shouldParseNumericString() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("42");

                assertEquals(42d, ImportUtils.getNumeric(row, 0));
            }
        }

        @Test
        void shouldReturnZeroWhenCellIsNull() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                assertEquals(0d, ImportUtils.getNumeric(row, 0));
            }
        }
    }

    @Nested
    @DisplayName("getNullableDouble")
    class GetNullableDouble {

        @Test
        void shouldReturnDouble() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue(2.5);

                assertEquals(2.5d, ImportUtils.getNullableDouble(row, 0));
            }
        }

        @Test
        void shouldReturnNullWhenBlank() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                assertNull(ImportUtils.getNullableDouble(row, 0));
            }
        }
    }

    @Nested
    @DisplayName("getNullableFloat")
    class GetNullableFloat {

        @Test
        void shouldReturnFloat() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue(3.5);

                assertEquals(3.5f, ImportUtils.getNullableFloat(row, 0));
            }
        }

        @Test
        void shouldReturnNullWhenBlank() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                assertNull(ImportUtils.getNullableFloat(row, 0));
            }
        }
    }

    @Nested
    @DisplayName("parseLocalDate")
    class ParseLocalDate {

        @Test
        void shouldParseDate() {
            assertEquals(LocalDate.of(2026, 10, 13), ImportUtils.parseLocalDate("2026-10-13"));
        }

        @Test
        void shouldReturnNullWhenBlank() {
            assertNull(ImportUtils.parseLocalDate(""));
        }

        @Test
        void shouldReturnNullWhenNull() {
            assertNull(ImportUtils.parseLocalDate(null));
        }
    }

    @Nested
    @DisplayName("parseLocalTime")
    class ParseLocalTime {

        @Test
        void shouldParseTime() {
            assertEquals(LocalTime.of(10, 30, 0), ImportUtils.parseLocalTime("10:30:00"));
        }

        @Test
        void shouldReturnNullWhenBlank() {
            assertNull(ImportUtils.parseLocalTime(""));
        }

        @Test
        void shouldReturnNullWhenNull() {
            assertNull(ImportUtils.parseLocalTime(null));
        }
    }

    @Nested
    @DisplayName("getRequiredString")
    class GetRequiredString {

        @Test
        void shouldReturnValue() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("value");

                assertEquals("value", ImportUtils.getRequiredString(row, 0, "Name"));
            }
        }

        @Test
        void shouldThrowWhenMissing() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);

                assertThrows(IllegalArgumentException.class, () -> ImportUtils.getRequiredString(row, 0, "Name"));
            }
        }
    }

    @Nested
    @DisplayName("getRequiredInteger")
    class GetRequiredInteger {

        @Test
        void shouldReturnInteger() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue(15);

                assertEquals(15, ImportUtils.getRequiredInteger(row, 0, "Quantity"));
            }
        }

        @Test
        void shouldThrowWhenMissing() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);

                assertThrows(IllegalArgumentException.class, () -> ImportUtils.getRequiredInteger(row, 0, "Quantity"));
            }
        }
    }

    @Nested
    @DisplayName("getRequiredBigDecimal")
    class GetRequiredBigDecimal {

        @Test
        void shouldReturnBigDecimal() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue(99.99);

                assertEquals(BigDecimal.valueOf(99.99), ImportUtils.getRequiredBigDecimal(row, 0, "Price"));
            }
        }

        @Test
        void shouldThrowWhenMissing() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);

                assertThrows(IllegalArgumentException.class, () -> ImportUtils.getRequiredBigDecimal(row, 0, "Price"));
            }
        }
    }

    @Nested
    @DisplayName("getRequiredBoolean")
    class GetRequiredBoolean {

        @Test
        void shouldReturnTrue() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("true");

                assertTrue(ImportUtils.getRequiredBoolean(row, 0, "Active"));
            }
        }

        @Test
        void shouldReturnFalse() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("false");

                assertFalse(ImportUtils.getRequiredBoolean(row, 0, "Active"));
            }
        }

        @Test
        void shouldSupportFrenchValues() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);

                row.createCell(0).setCellValue("oui");
                assertTrue(ImportUtils.getRequiredBoolean(row, 0, "Active"));

                row.getCell(0).setCellValue("non");
                assertFalse(ImportUtils.getRequiredBoolean(row, 0, "Active"));
            }
        }

        @Test
        void shouldThrowWhenValueIsInvalid() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("invalid");

                assertThrows(IllegalArgumentException.class, () -> ImportUtils.getRequiredBoolean(row, 0, "Active"));
            }
        }
    }

    @Nested
    @DisplayName("getRequiredStatus")
    class GetRequiredStatus {

        @Test
        void shouldParseStatus() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("ACTIVE");

                assertEquals(Status.ACTIVE, ImportUtils.getRequiredStatus(row, 0));
            }
        }

        @Test
        void shouldParseStatusCaseInsensitive() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("active");

                assertEquals(Status.ACTIVE, ImportUtils.getRequiredStatus(row, 0));
            }
        }

        @Test
        void shouldThrowWhenInvalidStatus() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("INVALID");

                assertThrows(IllegalArgumentException.class, () -> ImportUtils.getRequiredStatus(row, 0));
            }
        }
    }

    @Nested
    @DisplayName("getRequiredCategory")
    class GetRequiredCategory {

        @Test
        void shouldReturnCategory() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue("A");

                assertEquals('A', ImportUtils.getRequiredCategory(row, 0));
            }
        }

        @Test
        void shouldThrowWhenMissing() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);

                assertThrows(IllegalArgumentException.class, () -> ImportUtils.getRequiredCategory(row, 0));
            }
        }
    }

    @Nested
    @DisplayName("getNullableLong")
    class GetNullableLong {

        @Test
        void shouldReturnLong() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);
                row.createCell(0).setCellValue(100);

                assertEquals(100L, ImportUtils.getNullableLong(row, 0));
            }
        }

        @Test
        void shouldReturnNullWhenBlank() throws IOException {
            try (var workbook = new XSSFWorkbook()) {
                Row row = workbook.createSheet().createRow(0);

                assertNull(ImportUtils.getNullableLong(row, 0));
            }
        }
    }
}
