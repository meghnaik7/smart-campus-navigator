package com.megh.smartcampus.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
            .info(new Info().title("Smart Campus Navigator API").version("1.0.0")
                .description("Campus navigation with JWT auth, Dijkstra routing, Trie search"))
            .addSecurityItem(new SecurityRequirement().addList(scheme))
            .components(new Components().addSecuritySchemes(scheme,
                new SecurityScheme().name(scheme).type(SecurityScheme.Type.HTTP)
                    .scheme("bearer").bearerFormat("JWT")))
            .servers(List.of(new io.swagger.v3.oas.models.servers.Server()
                .url("http://localhost:8080").description("Local")));
    }
}
