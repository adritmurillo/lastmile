package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ShopifyWebhookRequest {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("shipping_address")
    private ShopifyAddress shippingAddress;

    @JsonProperty("line_items")
    private List<ShopifyLineItem> lineItems;

    @JsonProperty("total_weight")
    private Double totalWeightGrams;

    @JsonProperty("tags")
    private String tags;

    @Getter
    @NoArgsConstructor
    public static class ShopifyAddress {
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        @JsonProperty("phone")
        private String phone;
        @JsonProperty("address1")
        private String address1;
        @JsonProperty("city")
        private String city;
        @JsonProperty("province")
        private String province;
    }

    @Getter
    @NoArgsConstructor
    public static class ShopifyLineItem {
        @JsonProperty("title")
        private String title;
        @JsonProperty("quantity")
        private Integer quantity;
        @JsonProperty("grams")
        private Double grams;
    }
}