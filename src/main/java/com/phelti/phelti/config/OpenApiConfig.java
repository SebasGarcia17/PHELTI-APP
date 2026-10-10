package com.phelti.phelti.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pheltiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("PHELTI API")
                        .description("API REST para la Plataforma de Intercambio de Tiempo")
                        .version("v1"));
    }
}
