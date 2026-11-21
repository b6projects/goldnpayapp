package com.gps.os.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI goldNPayOpsOpenApi() {
        return new OpenAPI()
                .info(new Info().title("GoldNPay Ops API").version("v1")
                        .description("Outbound communication and background jobs. Called by Core only."))
                .components(new Components().addSecuritySchemes("apiKey",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER).name("X-API-KEY")))
                .addSecurityItem(new SecurityRequirement().addList("apiKey"));
    }
}
