package likelion.mlb.backendProject.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secretKey;

  private static final long ACCESS_TOKEN_VALID_TIME = 60 * 60 * 1000L; // 1시간
  private static final long REFRESH_TOKEN_VALID_TIME = 60 * 60 * 24 * 14 * 1000L; // 14일

  private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  public String createAccessToken(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALID_TIME))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String createRefreshToken(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALID_TIME))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getEmail(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

}
