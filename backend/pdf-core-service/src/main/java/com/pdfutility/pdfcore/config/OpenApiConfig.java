package com.pdfutility.pdfcore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pdfCoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PDF Core Service API")
                        .description("Enterprise PDF utility service providing merge, compress, and edit operations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PDF Utility Team")
                                .email("support@pdfutility.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Development server"),
                        new Server()
                                .url("https://api.pdfutility.com")
                                .description("Production server")
                ));
    }
}
