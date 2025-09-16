package com.tsu.tsueventhubapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info().title("TSU EventHub API").version("1.0").description(
                        "Документация REST API для проекта TSU EventHub"
                ))
                .addServersItem(new Server()
                        .url("https://api.tsu-eventhub.orexi4.ru:8443")
                        .description("Production server with HTTPS"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}