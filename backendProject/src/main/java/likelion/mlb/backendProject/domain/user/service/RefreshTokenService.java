package likelion.mlb.backendProject.domain.user.service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RedisTemplate<String, String> redisTemplate;
  private static final long REFRESH_TOKEN_EXPIRATION = 60 * 60 * 24 * 14L; // 14일

  public void save(String userId, String refreshToken) {
    String key = "refresh:" + userId;
    try {
      redisTemplate.opsForValue()
          .set(key, refreshToken, REFRESH_TOKEN_EXPIRATION, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new RuntimeException("리프레시 토큰 저장 실패: " + e.getMessage(), e);
    }
  }

  public Optional<String> get(String userId) {
    String token = redisTemplate.opsForValue().get("refresh:" + userId);
    return Optional.ofNullable(token);
  }

  public void delete(String userId) {
    redisTemplate.delete("refresh:" + userId);
  }

  public boolean isSameToken(String userId, String token) {
    return token.equals(redisTemplate.opsForValue().get("refresh:" + userId));
  }
}

