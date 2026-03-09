package com.lastmile.infrastructure.adapter.in.file;

import com.lastmile.domain.model.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CsvOrderParser {

    public List<Order> parse(InputStream inputStream) {
        List<Order> orders = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            // Skip header row
            reader.readNext();

            String[] line;
            int rowIndex = 1;

            while ((line = reader.readNext()) != null) {
                rowIndex++;

                if (isLineEmpty(line)) continue;

                try {
                    Order order = parseLine(line);
                    orders.add(order);
                } catch (Exception e) {
                    log.warn("Failed to parse CSV row {}. Skipping. Error: {}",
                            rowIndex, e.getMessage());
                }
            }

            log.info("CSV parsing completed. {} orders parsed.", orders.size());

        } catch (Exception e) {
            log.error("Failed to parse CSV file: {}", e.getMessage());
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }

        return orders;
    }


    private Order parseLine(String[] line) {
        return Order.builder()
                .externalTrackingCode(getValue(line, 0))
                .platformOrderNumber(getValue(line, 1))
                .recipientName(getValue(line, 2))
                .recipientPhone(getValue(line, 3))
                .addressText(getValue(line, 4))
                .weightKg(getDoubleValue(line, 5))
                .volumeCm3(getDoubleValue(line, 6))
                .priority(parsePriority(getValue(line, 7)))
                .deliveryDeadline(parseDate(getValue(line, 8)))
                .notes(getValue(line, 9))
                .deliveryAttempts(0)
                .createdAt(LocalDateTime.now())
                .loadSource(LoadSource.FILE)
                .build();
    }

    private String getValue(String[] line, int index) {
        if (index >= line.length) return null;
        String value = line[index].trim();
        return value.isEmpty() ? null : value;
    }

    private Double getDoubleValue(String[] line, int index) {
        String value = getValue(line, index);
        if (value == null) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
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

    private boolean isLineEmpty(String[] line) {
        for (String value : line) {
            if (value != null && !value.trim().isEmpty()) return false;
        }
        return true;
    }
}