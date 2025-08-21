package likelion.mlb.backendProject.global.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import likelion.mlb.backendProject.domain.user.service.RefreshTokenService;
import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Value("${frontend.https.url:${FRONTEND_HTTPS_URL:http://localhost:5173}}")
  private String frontendHttpUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String userId = oAuth2User.getAttribute("email"); // 또는 sub, id 등 서비스에 따라 다름

    String accessToken = jwtTokenProvider.createAccessToken(userId);
    String refreshToken = jwtTokenProvider.createRefreshToken(userId);

    refreshTokenService.save(userId, refreshToken);

    // 리프레시 토큰을 httpOnly 쿠키에 저장
    Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(request.isSecure()); // HTTPS 환경에서만 쿠키 전송
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 2주
    response.addCookie(refreshTokenCookie);

    // 액세스 토큰은 URL 파라미터로 전달
    String targetUrl =  frontendHttpUrl + "/auth/callback?accessToken=" + accessToken;
    response.sendRedirect(targetUrl);
  }
}