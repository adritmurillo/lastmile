package com.lastmile.infrastructure.adapter.in.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WooCommerceWebhookRequest {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("number")
    private String number;

    @JsonProperty("billing")
    private WooAddress billing;

    @JsonProperty("shipping")
    private WooAddress shipping;

    @JsonProperty("line_items")
    private List<WooLineItem> lineItems;

    @JsonProperty("meta_data")
    private List<WooMeta> metaData;

    @Getter
    @NoArgsConstructor
    public static class WooAddress {
        @JsonProperty("first_name")
        private String firstName;
        @JsonProperty("last_name")
        private String lastName;
        @JsonProperty("phone")
        private String phone;
        @JsonProperty("address_1")
        private String address1;
        @JsonProperty("city")
        private String city;
        @JsonProperty("state")
        private String state;
        @JsonProperty("email")
        private String email;
    }

    @Getter
    @NoArgsConstructor
    public static class WooLineItem {
        @JsonProperty("name")
        private String name;
        @JsonProperty("quantity")
        private Integer quantity;
    }

    @Getter
    @NoArgsConstructor
    public static class WooMeta {
        @JsonProperty("key")
        private String key;
        @JsonProperty("value")
        private String value;
    }
}