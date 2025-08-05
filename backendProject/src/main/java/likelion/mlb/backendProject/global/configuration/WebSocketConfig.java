package likelion.mlb.backendProject.global.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket/STOMP 설정 클래스
 * - 클라이언트는 /ws-chat 엔드포인트로 연결
 * - 메시지는 /app 접두어로 들어와서, /topic 브로커로 전달됨
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final MatchHandler matchHandler;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
      registry.addHandler(matchHandler, "/ws/match")
              .setAllowedOrigins("*");
  }
  /**
   * 메시지 브로커 설정
   * - enableSimpleBroker: 서버에서 구독한 클라이언트로 메시지를 브로드캐스트할 경로
   * - setApplicationDestinationPrefixes: 클라이언트 메시지가
   * @MessageMapping으로 라우팅될 경로
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  /**
   * STOMP 엔드포인트 등록
   * - /ws-chat: WebSocket 연결을 위한 엔드포인트 - withSockJS(): SockJS 폴백 옵션 활성화
   * - setAllowedOriginPatterns("*"):
   * 모든 출처 허용 (필요 시 도메인 제한)
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-chat")
        .setAllowedOriginPatterns("*")
        .withSockJS();
  }
}

