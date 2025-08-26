package likelion.mlb.backendProject.global.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
            .title("MLB Project API")
            .version("v1.0.0")
            .description("API documentation for the MLB project.");

        // Security Scheme for JWT
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
            .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP) // HTTP 방식
                .scheme("bearer")
                .bearerFormat("JWT"));

        return new OpenAPI()
            .info(info)
            .addSecurityItem(securityRequirement)
            .components(components);
    }

    @Bean
    public GroupedOpenApi chatApi() {
        return GroupedOpenApi.builder()
            .group("Chat API")
            .pathsToMatch("/api/chat-rooms/**", "/api/notify/**")
            .packagesToScan("likelion.mlb.backendProject.domain.chat")
            .build();
    }

    @Bean
    public GroupedOpenApi chatSearchApi() {
        return GroupedOpenApi.builder()
            .group("Chat Search API")
            .pathsToMatch("/api/chat-rooms/*/search/**", "/api/chat-rooms/*/reindex/**")
            .packagesToScan("likelion.mlb.backendProject.domain.chat.elasticsearch")
            .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("User API")
            .pathsToMatch("/api/user/**", "/api/auth/**")
            .packagesToScan("likelion.mlb.backendProject.domain.user")
            .build();
    }

    @Bean
    public GroupedOpenApi draftApi() {
        return GroupedOpenApi.builder()
            .group("Draft API")
            .pathsToMatch("/api/draft/**")
            .packagesToScan("likelion.mlb.backendProject.domain.draft")
            .build();
    }

    @Bean
    public GroupedOpenApi playerApi() {
        return GroupedOpenApi.builder()
            .group("Player API")
            .pathsToMatch("/api/player/**", "/api/elementType/**")
            .packagesToScan("likelion.mlb.backendProject.domain.player")
            .build();
    }

    @Bean
    public GroupedOpenApi playerCacheApi() {
        return GroupedOpenApi.builder()
            .group("Player Cache API")
            .pathsToMatch("/api/playerCache/**")
            .packagesToScan("likelion.mlb.backendProject.domain.player.cache")
            .build();
    }

    @Bean
    public GroupedOpenApi playerEsApi() {
        return GroupedOpenApi.builder()
            .group("Player Search API")
            .pathsToMatch("/api/playerEs/**")
            .packagesToScan("likelion.mlb.backendProject.domain.player.elasticsearch")
            .build();
    }

    @Bean
    public GroupedOpenApi matchApi() {
        return GroupedOpenApi.builder()
            .group("Match API")
            .pathsToMatch("/api/match/**")
            .packagesToScan("likelion.mlb.backendProject.domain.match")
            .build();
    }

    @Bean
    public GroupedOpenApi teamApi() {
        return GroupedOpenApi.builder()
            .group("Team API")
            .pathsToMatch("/api/team/**")
            .packagesToScan("likelion.mlb.backendProject.domain.team")
            .build();
    }

    @Bean
    public GroupedOpenApi roundApi() {
        return GroupedOpenApi.builder()
            .group("Round API")
            .pathsToMatch("/api/round/**")
            .packagesToScan("likelion.mlb.backendProject.domain.round")
            .build();
    }

}