package com.lastmile.infrastructure.adapter.in.rest;

import com.lastmile.application.usecase.dto.OrderDto;
import com.lastmile.domain.model.Order;
import com.lastmile.domain.port.in.ProcessWebhookUseCase;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.ShopifyWebhookRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.WooCommerceWebhookRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.ApiResponse;
import com.lastmile.infrastructure.adapter.in.rest.dto.response.OrderResponse;
import com.lastmile.infrastructure.adapter.in.rest.mapper.OrderDomainMapper;
import com.lastmile.infrastructure.adapter.in.rest.mapper.OrderRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Receive orders automatically from e-commerce platforms")
public class WebhookController {

    private final ProcessWebhookUseCase processWebhookUseCase;
    private final OrderDomainMapper orderDomainMapper;
    private final OrderRestMapper orderRestMapper;

    @PostMapping("/shopify")
    @Operation(summary = "Receive Shopify order webhook",
            description = "Automatically creates an order when a Shopify sale is made")
    public ResponseEntity<ApiResponse<OrderResponse>> shopifyWebhook(
            @RequestBody ShopifyWebhookRequest request) {

        Order order = processWebhookUseCase.processShopifyOrder(request);
        OrderDto dto = orderDomainMapper.toDto(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderRestMapper.toResponse(dto)));
    }

    @PostMapping("/woocommerce")
    @Operation(summary = "Receive WooCommerce order webhook",
            description = "Automatically creates an order when a WooCommerce sale is made")
    public ResponseEntity<ApiResponse<OrderResponse>> wooCommerceWebhook(
            @RequestBody WooCommerceWebhookRequest request) {

        Order order = processWebhookUseCase.processWooCommerceOrder(request);
        OrderDto dto = orderDomainMapper.toDto(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderRestMapper.toResponse(dto)));
    }
}