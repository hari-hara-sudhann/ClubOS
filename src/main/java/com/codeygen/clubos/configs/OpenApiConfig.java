package com.codeygen.clubos.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clubOsOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("ClubOS API")
                        .description(
                                "OpenAPI documentation for the ClubOS backend. " +
                                "This API models governance-heavy workflows around member onboarding, task assignment, bidding, ownership, and submission review. " +
                                "Authentication is still under development, so some endpoints are documented ahead of full auth enforcement."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CodeyGen")
                                .email("support@codeygen.dev"))
                        .license(new License()
                                .name("MIT License")))

                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Bearer token scheme reserved for the upcoming authentication module.")));
    }
}
