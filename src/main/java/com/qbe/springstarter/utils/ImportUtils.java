package com.qbe.springstarter.utils;

import com.qbe.springstarter.enums.Status;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

public class ImportUtils {

    private ImportUtils() {}

    public static String getString(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? "" : cell.toString().trim();
    }

    public static double getNumeric(Row row, int index) {
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

    public static Double getNullableDouble(Row row, int index) {
        String value = getString(row, index);
        return value.isBlank() ? null : getNumeric(row, index);
    }

    public static Float getNullableFloat(Row row, int index) {
        String value = getString(row, index);
        return value.isBlank() ? null : (float) getNumeric(row, index);
    }

    public static LocalDate parseLocalDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    public static LocalTime parseLocalTime(String value) {
        return value == null || value.isBlank() ? null : LocalTime.parse(value);
    }

    public static String getRequiredString(Row row, int index, String fieldName) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is mandatory");
        }
        return value;
    }

    public static Integer getRequiredInteger(Row row, int index, String fieldName) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is mandatory");
        }
        return (int) getNumeric(row, index);
    }

    public static BigDecimal getRequiredBigDecimal(Row row, int index, String fieldName) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is mandatory");
        }
        return BigDecimal.valueOf(getNumeric(row, index));
    }

    public static Boolean getRequiredBoolean(Row row, int index, String fieldName) {
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

    public static Status getRequiredStatus(Row row, int index) {
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

    public static Character getRequiredCategory(Row row, int index) {
        String value = getString(row, index);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Category is mandatory");
        }
        return value.charAt(0);
    }

    public static Long getNullableLong(Row row, int index) {
        String value = getString(row, index);
        if (value.isBlank()) {
            return null;
        }
        return (long) getNumeric(row, index);
    }
}
