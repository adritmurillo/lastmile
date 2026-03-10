package com.lastmile.infrastructure.adapter.in.file;

import com.lastmile.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExcelOrderParser {

    @Value("${lastmile.orders.chunk-size-load:500}")
    private int chunkSize;

    public List<Order> parse(InputStream inputStream) {
        List<Order> orders = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows();

            log.info("Starting Excel parsing. Total rows (including header): {}", totalRows);

            for (int rowIndex = 1; rowIndex < totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    Order order = parseRow(row);
                    orders.add(order);
                } catch (Exception e) {
                    log.warn("Failed to parse row {}. Skipping. Error: {}", rowIndex + 1, e.getMessage());
                }

                if (rowIndex % chunkSize == 0) {
                    log.info("Parsed {}/{} rows", rowIndex, totalRows - 1);
                }
            }

            log.info("Excel parsing completed. {} orders parsed successfully.", orders.size());

        } catch (Exception e) {
            log.error("Failed to parse Excel file: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        return orders;
    }

    private Order parseRow(Row row) {
        return Order.builder()
                .externalTrackingCode(getStringValue(row, 0))
                .recipientName(getStringValue(row, 1))
                .recipientPhone(getStringValue(row, 2))
                .addressText(getStringValue(row, 3))
                .weightKg(getDoubleValue(row, 4))
                .volumeCm3(getDoubleValue(row, 5))
                .priority(parsePriority(getStringValue(row, 6)))
                .deliveryDeadline(parseDateFromCell(row, 7))
                .deliveryAttempts(0)
                .createdAt(LocalDateTime.now())
                .loadSource(LoadSource.FILE)
                .build();
    }

    private String getStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Double getDoubleValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private OrderPriority parsePriority(String value) {
        if (value == null) return OrderPriority.STANDARD;
        return switch (value.toUpperCase().trim()) {
            case "EXPRESS" -> OrderPriority.EXPRESS;
            default -> OrderPriority.STANDARD;
        };
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            log.warn("Could not parse date: {}", value);
            return null;
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 9; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private LocalDate parseDateFromCell(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        return parseDate(getStringValue(row, cellIndex));
    }
}