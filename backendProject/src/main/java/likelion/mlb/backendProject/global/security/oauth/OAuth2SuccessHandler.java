package likelion.mlb.backendProject.global.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import likelion.mlb.backendProject.domain.user.service.RefreshTokenService;
import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String userId = oAuth2User.getAttribute("email"); // 또는 sub, id 등 서비스에 따라 다름

    String accessToken = jwtTokenProvider.createAccessToken(userId);
    String refreshToken = jwtTokenProvider.createRefreshToken(userId);

    refreshTokenService.save(userId, refreshToken);

    /* ✅ [임시: HTML 테스트용] 토큰을 세션에 저장 */
    request.getSession().setAttribute("accessToken", accessToken);
    request.getSession().setAttribute("refreshToken", refreshToken);

    response.sendRedirect("/login-success.html");

    /* ✅ [후에 React 연동 시 사용할 JSON 응답 코드]
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    Map<String, String> tokens = Map.of(
        "accessToken", accessToken,
        "refreshToken", refreshToken
    );

    new ObjectMapper().writeValue(response.getWriter(), tokens);
    */
  }
}
