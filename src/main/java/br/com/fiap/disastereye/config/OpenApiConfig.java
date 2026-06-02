package br.com.fiap.disastereye.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DisasterEye API")
                        .description("""
                                **DisasterEye** é uma plataforma de prevenção e monitoramento de desastres naturais
                                que integra dados de satélites da NASA (EONET) para identificar e alertar sobre
                                eventos como incêndios, inundações, vulcões e tempestades em tempo real.
                                
                                **ODS 9** — Indústria, Inovação e Infraestrutura | FIAP 3ESPR 2026
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe DisasterEye - FIAP")
                                .email("disastereye@fiap.com.br"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT obtido no endpoint /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .tags(List.of(
                        new Tag().name("Autenticação").description("Registro e login de usuários"),
                        new Tag().name("Alertas").description("CRUD de alertas de desastres"),
                        new Tag().name("NASA EONET").description("Integração com API de eventos da NASA"),
                        new Tag().name("Relatórios").description("Relatórios de campo enviados por usuários"),
                        new Tag().name("Dashboard").description("Estatísticas e métricas da plataforma"),
                        new Tag().name("Usuários").description("Gerenciamento de usuários (ADMIN)")
                ));
    }
}
