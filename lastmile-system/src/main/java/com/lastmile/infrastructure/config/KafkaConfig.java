package com.lastmile.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public static final String TOPIC_ORDER_LOADED = "order.loaded";
    public static final String TOPIC_ROUTE_CONFIRMED = "route.confirmed";
    public static final String TOPIC_DELIVERY_COMPLETED = "delivery.completed";
    public static final String TOPIC_DELIVERY_FAILED = "delivery.failed";

    @Bean
    public NewTopic orderLoadedTopic(){
        return TopicBuilder.name(TOPIC_ORDER_LOADED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic routeConfirmedTopic(){
        return TopicBuilder.name(TOPIC_ROUTE_CONFIRMED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deliveryCompletedTopic(){
        return TopicBuilder.name(TOPIC_DELIVERY_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic deliveryFailedTopic(){
        return TopicBuilder.name(TOPIC_DELIVERY_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
