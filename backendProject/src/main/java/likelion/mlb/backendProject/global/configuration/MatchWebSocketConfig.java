package likelion.mlb.backendProject.global.configuration;

import likelion.mlb.backendProject.domain.match.handler.MatchHandler;
import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class MatchWebSocketConfig implements WebSocketConfigurer {

    private final MatchHandler matchHandler;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${frontend.http.url:${FRONTEND_HTTP_URL:http://localhost:5173}}")
    private String frontendHttpUrl;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(matchHandler, "/ws/match")
                .setHandshakeHandler(new AuthHandshakeHandler(jwtTokenProvider)) // ✅ 여기 한 줄
                .setAllowedOrigins(frontendHttpUrl);
    }
}
