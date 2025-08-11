package likelion.mlb.backendProject.global.configuration;

import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@RequiredArgsConstructor
public class AuthHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {

        // 1) 세션/쿠키 기반: SecurityContext에 이미 인증이 있으면 그걸 그대로 사용
        Authentication ctxAuth = SecurityContextHolder.getContext().getAuthentication();
        if (ctxAuth != null && ctxAuth.isAuthenticated()
                && !(ctxAuth instanceof AnonymousAuthenticationToken)) {
            return ctxAuth; // Authentication 자체가 Principal
        }

        // 2) JWT 기반: 쿼리 파라미터 token 로 들어온 경우 검증 후 Authentication 생성
        if (request instanceof ServletServerHttpRequest servlet) {
            String token = servlet.getServletRequest().getParameter("token");
            if (token != null && !token.isBlank()) {
                if (token.startsWith("Bearer ")) token = token.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token); // 너희 구현 그대로 사용
                    return auth; // Authentication 반환 → session.getPrincipal()로 접근 가능
                }
            }
        }

        // 인증 정보가 없으면 null → 이후 핸들러에서 거절
        return null;
    }
}