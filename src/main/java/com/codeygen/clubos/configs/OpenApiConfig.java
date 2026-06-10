package com.codeygen.clubos.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
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
                                "Authentication is handled via Google OAuth2. \n\n" +
                                "### Login Flow:\n" +
                                "1. Navigate to `/oauth2/authorization/google` in your browser.\n" +
                                "2. Upon successful login and email verification, the server returns a JSON with a `token`.\n" +
                                "3. Use this token in the `Authorization` header as `Bearer <token>` for all other requests.\n" +
                                "Note: Only emails registered in the database are allowed to login."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CodeyGen")
                                .email("support@codeygen.dev"))
                        .license(new License()
                                .name("MIT License")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token here to authorize requests.")));
    }
}
