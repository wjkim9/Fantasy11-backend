package likelion.mlb.backendProject.global.configuration;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import likelion.mlb.backendProject.domain.match.dto.AssignDto;
import likelion.mlb.backendProject.domain.match.ws.message.DraftStartMessage;
import likelion.mlb.backendProject.domain.match.dto.MatchStatusResponse;
import likelion.mlb.backendProject.domain.match.ws.message.StatusMessage;
import likelion.mlb.backendProject.domain.match.ws.message.UserIdMessage;
import likelion.mlb.backendProject.domain.match.dto.RoundInfo;

/**
 * HTTP 엔드포인트에서 직접 사용하지 않는 WebSocket 전용 DTO를
 * Swagger(OpenAPI) Schemas 섹션에 등록하기 위한 설정.
 *
 * 요구사항:
 * - Spring Boot 3.x + springdoc-openapi-starter-webmvc-ui 2.x
 * - (권장) spring-boot-starter-validation (jakarta.validation 반영)
 *
 * build.gradle 예시:
 * implementation "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0"
 * implementation "org.springframework.boot:spring-boot-starter-validation"
 */
@Configuration
public class OpenApiWsSchemasConfig {

    /**
     * springdoc 스펙 생성 시점에 WS DTO들을 components.schemas에 주입합니다.
     */
    @Bean
    public OpenApiCustomizer wsSchemasCustomizer() {
        return openApi -> {
            registerSchemas(openApi,
                    AssignDto.class,
                    DraftStartMessage.class,
                    MatchStatusResponse.class,
                    StatusMessage.class,
                    UserIdMessage.class,
                    RoundInfo.class
            );
        };
    }


    /**
     * 지정한 클래스(들)를 swagger-core ModelConverters로 변환하여
     * openApi.components.schemas에 등록합니다.
     */
    private void registerSchemas(OpenAPI openApi, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            ModelConverters.getInstance().read(clazz)
                    .forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));
        }
    }
}