package com.lastmile.application.usecase;

import com.lastmile.domain.model.*;
import com.lastmile.domain.port.in.ManageOrdersUseCase;
import com.lastmile.domain.port.in.ProcessWebhookUseCase;
import com.lastmile.domain.service.OrderDomainService;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.ShopifyWebhookRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.WooCommerceWebhookRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessWebhookUseCaseImpl implements ProcessWebhookUseCase {

    private final ManageOrdersUseCase manageOrdersUseCase;
    private final OrderDomainService orderDomainService;

    @Override
    public Order processShopifyOrder(ShopifyWebhookRequest request) {
        log.info("Processing Shopify webhook for order: {}", request.getOrderNumber());

        ShopifyWebhookRequest.ShopifyAddress addr = request.getShippingAddress();
        String fullName = addr.getFirstName() + " " + addr.getLastName();
        String fullAddress = addr.getAddress1() + " " + addr.getCity() + " " + addr.getProvince();


        double weightKg = request.getTotalWeightGrams() != null
                ? request.getTotalWeightGrams() / 1000.0 : 1.0;
        double volumeCm3 = weightKg * 2500;

        boolean isExpress = request.getTags() != null &&
                request.getTags().toLowerCase().contains("express");

        Order order = Order.builder()
                .externalTrackingCode("SHOPIFY-" + request.getOrderNumber())
                .platformOrderNumber(String.valueOf(request.getId()))
                .recipientName(fullName)
                .recipientPhone(addr.getPhone() != null ? addr.getPhone() : "N/A")
                .recipientEmail(request.getEmail())
                .addressText(fullAddress)
                .weightKg(weightKg)
                .volumeCm3(volumeCm3)
                .priority(isExpress ? OrderPriority.EXPRESS : OrderPriority.STANDARD)
                .deliveryDeadline(LocalDate.now().plusDays(3))
                .loadSource(LoadSource.WEBHOOK)
                .build();

        return manageOrdersUseCase.createOrder(order);
    }

    @Override
    public Order processWooCommerceOrder(WooCommerceWebhookRequest request) {
        log.info("Processing WooCommerce webhook for order: {}", request.getNumber());

        WooCommerceWebhookRequest.WooAddress addr = request.getShipping() != null
                ? request.getShipping() : request.getBilling();
        String fullName = addr.getFirstName() + " " + addr.getLastName();
        String fullAddress = addr.getAddress1() + " " + addr.getCity() + " " + addr.getState();

        String deadlineStr = request.getMetaData() != null ? request.getMetaData().stream()
                .filter(m -> "_delivery_deadline".equals(m.getKey()))
                .map(WooCommerceWebhookRequest.WooMeta::getValue)
                .findFirst().orElse(null) : null;

        LocalDate deadline = deadlineStr != null
                ? LocalDate.parse(deadlineStr) : LocalDate.now().plusDays(3);

        String priorityStr = request.getMetaData() != null ? request.getMetaData().stream()
                .filter(m -> "_priority".equals(m.getKey()))
                .map(WooCommerceWebhookRequest.WooMeta::getValue)
                .findFirst().orElse("STANDARD") : "STANDARD";

        OrderPriority priority = "EXPRESS".equalsIgnoreCase(priorityStr)
                ? OrderPriority.EXPRESS : OrderPriority.STANDARD;

        Order order = Order.builder()
                .externalTrackingCode("WOO-" + request.getNumber())
                .platformOrderNumber(String.valueOf(request.getId()))
                .recipientName(fullName)
                .recipientPhone(addr.getPhone() != null ? addr.getPhone() : "N/A")
                .recipientEmail(addr.getEmail())
                .addressText(fullAddress)
                .weightKg(1.0)
                .volumeCm3(2500.0)
                .priority(priority)
                .deliveryDeadline(deadline)
                .loadSource(LoadSource.WEBHOOK)
                .build();

        return manageOrdersUseCase.createOrder(order);
    }
}