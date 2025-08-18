/*
package likelion.mlb.backendProject.global.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider; // 네 클래스 사용

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

  private final @Lazy JwtTokenProvider jwtTokenProvider;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String auth = accessor.getFirstNativeHeader("Authorization");
      if (auth != null && auth.startsWith("Bearer ")) {
        String token = auth.substring(7);
        Authentication authentication = jwtTokenProvider.getAuthentication(token); // 이미 제공됨
        accessor.setUser(authentication);
      }
      // 없으면 익명 허용(테스트 편의). 운영 시 예외 던지기
    }
    return message;
  }
}
*/
