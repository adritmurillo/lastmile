package com.lastmile.application.usecase;

import com.lastmile.domain.exception.BulkLoadNotFoundException;
import com.lastmile.domain.model.*;
import com.lastmile.domain.port.in.LoadOrdersUseCase;
import com.lastmile.domain.port.out.BulkLoadRepository;
import com.lastmile.domain.port.out.GeocodingPort;
import com.lastmile.domain.port.out.OrderRepository;
import com.lastmile.domain.service.OrderDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lastmile.infrastructure.adapter.in.file.CsvOrderParser;
import com.lastmile.infrastructure.adapter.in.file.ExcelOrderParser;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadOrdersUseCaseImpl implements LoadOrdersUseCase {

    private final OrderRepository orderRepository;
    private final BulkLoadRepository bulkLoadRepository;
    private final GeocodingPort geocodingPort;
    private final OrderDomainService orderDomainService;
    private final CsvOrderParser csvOrderParser;
    private final ExcelOrderParser excelOrderParser;

    @Override
    @Transactional
    public UUID startFileLoad(InputStream file, String fileName, String uploadedBy) {

        BulkLoad bulkLoad = BulkLoad.builder()
                .id(UUID.randomUUID())
                .fileName(fileName)
                .loadSource(LoadSource.FILE)
                .status(BulkLoadStatus.IN_PROGRESS)
                .totalRecords(0)
                .successfulRecords(0)
                .failedRecords(0)
                .startedAt(LocalDateTime.now())
                .uploadedBy(uploadedBy)
                .build();

        BulkLoad savedBulkLoad = bulkLoadRepository.save(bulkLoad);

        processFileAsync(file, savedBulkLoad.getId());

        return savedBulkLoad.getId();
    }

    @Override
    public BulkLoad getBulkLoadStatus(UUID bulkLoadId) {
        return bulkLoadRepository.findById(bulkLoadId)
                .orElseThrow(() -> new BulkLoadNotFoundException(bulkLoadId));
    }

    @Async
    @Transactional
    public void processFileAsync(InputStream file, UUID bulkLoadId) {
        log.info("Starting async file processing for bulkLoadId: {}", bulkLoadId);

        BulkLoad bulkLoad = bulkLoadRepository.findById(bulkLoadId)
                .orElseThrow(() -> new BulkLoadNotFoundException(bulkLoadId));

        try {
            List<Order> parsedOrders = parseFile(file, bulkLoad.getFileName());

            Set<String> existingCodes = parsedOrders.stream()
                    .map(Order::getExternalTrackingCode)
                    .filter(code -> orderRepository.existsByExternalTrackingCode(code))
                    .collect(Collectors.toSet());

            OrderDomainService.ValidationResult result =
                    orderDomainService.validateOrders(parsedOrders, existingCodes);

            List<Order> geocodedOrders = result.validOrders().stream()
                    .map(this::geocodeOrder)
                    .collect(Collectors.toList());

            List<Order> readyOrders = geocodedOrders.stream()
                    .map(order -> order
                            .withId(UUID.randomUUID())
                            .withTrackingCode(orderDomainService.generateTrackingCode())
                            .withStatus(OrderStatus.PENDING))
                    .collect(Collectors.toList());

            orderRepository.saveAll(readyOrders);

            BulkLoad completed = bulkLoad
                    .withStatus(result.hasErrors()
                            ? BulkLoadStatus.COMPLETED_WITH_ERRORS
                            : BulkLoadStatus.COMPLETED)
                    .withTotalRecords(parsedOrders.size())
                    .withSuccessfulRecords(readyOrders.size())
                    .withFailedRecords(result.errors().size())
                    .withErrors(result.errors())
                    .withFinishedAt(LocalDateTime.now());

            bulkLoadRepository.save(completed);
            log.info("File processing completed for bulkLoadId: {}. Success: {}, Failed: {}",
                    bulkLoadId, readyOrders.size(), result.errors().size());

        } catch (Exception e) {
            log.error("File processing failed for bulkLoadId: {}", bulkLoadId, e);

            BulkLoad failed = bulkLoad
                    .withStatus(BulkLoadStatus.FAILED)
                    .withFinishedAt(LocalDateTime.now());

            bulkLoadRepository.save(failed);
        }
    }
    private Order geocodeOrder(Order order) {
        return geocodingPort.geocode(order.getAddressText())
                .map(coords -> order
                        .withLatitude(coords.latitude())
                        .withLongitude(coords.longitude()))
                .orElseGet(() -> {
                    log.warn("Could not geocode address for order: {}", order.getExternalTrackingCode());
                    return order;
                });
    }

    private List<Order> parseFile(InputStream file, String fileName) {
        if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
            return csvOrderParser.parse(file);
        } else if (fileName != null &&
                (fileName.toLowerCase().endsWith(".xlsx") ||
                        fileName.toLowerCase().endsWith(".xls"))) {
            return excelOrderParser.parse(file);
        } else {
            throw new RuntimeException("Unsupported file format. Use .csv or .xlsx");
        }
    }
}