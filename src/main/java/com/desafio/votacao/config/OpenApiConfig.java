package com.desafio.votacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI votacaoOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("API de Votação")
                                .description(
                                        "API REST para gerenciamento de pautas, " +
                                                "sessões de votação, votos e resultados."
                                )
                                .version("v1")
                );
    }
}
