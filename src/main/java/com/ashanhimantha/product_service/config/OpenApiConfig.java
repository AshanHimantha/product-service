package com.ashanhimantha.product_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Service API")
                        .description("RESTful API for Product Microservice - E-commerce Platform")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Ashan Himantha")
                                .email("ashanhimantha321@gmail.com")
                                .url("https://github.com/ashanhimantha"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                // no explicit servers() so Swagger UI uses the request origin (auto-maps to any host/port)
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token from AWS Cognito")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"));
    }
}

