package likelion.mlb.backendProject.global.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    // email 기준으로 userId 추출 (UUID 아님 주의)
    String userId = oAuth2User.getAttribute("email"); // 또는 sub, id 등 서비스에 따라 다름

    String accessToken = jwtTokenProvider.createAccessToken(userId);
    String refreshToken = jwtTokenProvider.createRefreshToken(userId);

    refreshTokenService.save(userId, refreshToken);

    /* ✅ [임시: HTML 테스트용 redirect] */
    // 추후 React 연결 시 이 부분 주석 처리하고 아래 JSON 응답으로 되돌리기
    String redirectUrl = "/login-success.html"
        + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
        + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

    response.sendRedirect(redirectUrl);



    /* ✅ 후에 react 쓸 때 사용할 코드 (Json 응답)  */
//    response.setContentType("application/json");
//    response.setCharacterEncoding("UTF-8");
//
//    Map<String, String> tokens = Map.of(
//        "accessToken", accessToken,
//        "refreshToken", refreshToken
//    );
//
//    new ObjectMapper().writeValue(response.getWriter(), tokens);
  }
}

