package com.lastmile.infrastructure.adapter.in.rest;

import com.lastmile.application.usecase.dto.OrderDto;
import com.lastmile.domain.model.OrderPriority;
import com.lastmile.domain.model.OrderStatus;
import com.lastmile.domain.port.in.LoadOrdersUseCase;
import com.lastmile.domain.port.in.ManageCouriersUseCase;
import com.lastmile.domain.port.in.ManageOrdersUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.CreateOrderRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.BulkLoadResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.OrderResponse;
import com.lastmile.infrastructure.adapter.in.rest.mapper.BulkLoadDomainMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.BulkLoadRestMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.OrderDomainMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.OrderRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management and bulk loading")
public class OrderController {
    private final ManageOrdersUseCase manageOrdersUseCase;
    private final LoadOrdersUseCase loadOrdersUseCase;
    private final OrderDomainMapper orderDomainMapper;
    private final OrderRestMapper orderRestMapper;
    private final BulkLoadDomainMapper bulkLoadDomainMapper;
    private final BulkLoadRestMapper bulkLoadRestMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @Operation(summary = "Get all orders", description = "Returns orders filtered by status, date and priority")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) OrderPriority priority) {

        List<OrderDto> orders = orderDomainMapper.toDtoList(
                manageOrdersUseCase.getOrders(status, date, priority));

        return ResponseEntity.ok(ApiResponse.ok(orderRestMapper.toResponseList(orders)));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @Operation(summary = "Get pending orders", description = "Returns all orders waiting to be assigned")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getPendingOrders() {

        List<OrderDto> orders = orderDomainMapper.toDtoList(
                manageOrdersUseCase.getPendingOrders());

        return ResponseEntity.ok(ApiResponse.ok(orderRestMapper.toResponseList(orders)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable UUID id) {

        return manageOrdersUseCase.getOrderById(id)
                .map(orderDomainMapper::toDto)
                .map(orderRestMapper::toResponse)
                .map(ApiResponse::ok)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tracking/{trackingCode}")
    @Operation(summary = "Get order by tracking code", description = "e.g: PKG-2024-A1B2C3D4")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByTrackingCode(
            @PathVariable String trackingCode) {

        return manageOrdersUseCase.getOrderByTrackingCode(trackingCode)
                .map(orderDomainMapper::toDto)
                .map(orderRestMapper::toResponse)
                .map(ApiResponse::ok)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload orders from Excel or CSV file",
            description = "Accepts .xlsx and .csv files. Returns a bulkLoadId to track progress.")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<UUID>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "system") String uploadedBy) throws IOException {

        UUID bulkLoadId = loadOrdersUseCase.startFileLoad(
                file.getInputStream(),
                file.getOriginalFilename(),
                uploadedBy);

        return ResponseEntity.accepted()
                .body(ApiResponse.ok(bulkLoadId));
    }

    @GetMapping("/upload/{bulkLoadId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @Operation(summary = "Check bulk load progress",
            description = "Returns the current status of a file upload including errors")
    public ResponseEntity<ApiResponse<BulkLoadResponse>> getBulkLoadStatus(
            @PathVariable UUID bulkLoadId) {

        BulkLoadResponse response = bulkLoadRestMapper.toResponse(
                bulkLoadDomainMapper.toDto(
                        loadOrdersUseCase.getBulkLoadStatus(bulkLoadId)));

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/reschedule-failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    @Operation(summary = "Reschedule failed orders",
            description = "Manually triggers rescheduling of failed orders for next delivery attempt")
    public ResponseEntity<ApiResponse<String>> rescheduleFailedOrders() {

        int count = manageOrdersUseCase.rescheduleFailedOrders();
        return ResponseEntity.ok(ApiResponse.ok(count + " orders rescheduled successfully"));
    }

    @PostMapping
    @Operation(summary = "Create a single order")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderDto dto = orderRestMapper.toDto(request);
        OrderDto saved = orderDomainMapper.toDto(
                manageOrdersUseCase.createOrder(
                        orderDomainMapper.toDomain(dto)));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderRestMapper.toResponse(saved)));
    }

}
