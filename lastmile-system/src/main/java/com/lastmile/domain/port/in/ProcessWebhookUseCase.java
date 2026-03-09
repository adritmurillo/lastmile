package com.lastmile.domain.port.in;

import com.lastmile.domain.model.Order;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.ShopifyWebhookRequest;
import com.lastmile.infrastructure.adapter.in.rest.dto.request.WooCommerceWebhookRequest;

public interface ProcessWebhookUseCase {
    Order processShopifyOrder(ShopifyWebhookRequest request);
    Order processWooCommerceOrder(WooCommerceWebhookRequest request);
}