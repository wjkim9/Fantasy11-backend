package likelion.mlb.backendProject.domain.user.controller;

import java.util.Map;
import likelion.mlb.backendProject.domain.user.service.RefreshTokenService;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("/reissue")
  public ResponseEntity<?> reissue(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");
    String userId = jwtTokenProvider.getEmail(refreshToken);

    if (!jwtTokenProvider.validateToken(refreshToken) || !refreshTokenService.isSameToken(userId, refreshToken)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }

    String newAccessToken = jwtTokenProvider.createAccessToken(userId);
    return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
    String userId = userDetails.getUser().getId().toString();
    refreshTokenService.delete(userId);
    return ResponseEntity.ok("로그아웃 완료");
  }
}

