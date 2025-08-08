package likelion.mlb.backendProject.global.configuration;

import likelion.mlb.backendProject.domain.match.handler.MatchHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class MatchWebSocketConfig implements WebSocketConfigurer {

    private final MatchHandler matchHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(matchHandler, "/ws/match") // ✅ 분리된 경로
                .setAllowedOrigins("*");
    }
}
