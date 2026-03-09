package com.lastmile.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Last Mile Delivery System API")
                        .version("1.0.0")
                        .description("REST API for managing last mile delivery operations " +
                                "including order management, route optimization, " +
                                "courier dispatch and real-time tracking.")
                        .contact(new Contact()
                                .name("Last Mile Team")));
    }
}