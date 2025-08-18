package likelion.mlb.backendProject.global.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.UUID;
import java.util.regex.Pattern;

import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;
import likelion.mlb.backendProject.domain.chat.repository.ChatMembershipRepository;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final ApplicationContext ctx; // ⬅️ 런타임 지연 조회용

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {

    // 1) CONNECT 시 JWT 인증 처리 (런타임 조회)
    ChannelInterceptor jwtConnect = new ChannelInterceptor() {
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc != null && StompCommand.CONNECT.equals(acc.getCommand())) {
          String auth = acc.getFirstNativeHeader("Authorization");
          if (auth != null && auth.startsWith("Bearer ")) {
            JwtTokenProvider jwt = ctx.getBean(JwtTokenProvider.class); // ⬅️ 지금 시점에만 조회
            Authentication authentication = jwt.getAuthentication(auth.substring(7));
            acc.setUser(authentication);
          }
        }
        return message;
      }
    };

    // 2) SUBSCRIBE 시 방 멤버 권한 체크 (런타임 조회)
    ChannelInterceptor subscribeAuth = new ChannelInterceptor() {
      private final Pattern CHAT_TOPIC = Pattern.compile("^/topic/chat/([0-9a-fA-F-]{36})$");
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc != null && StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
          String dest = acc.getDestination();
          if (dest != null) {
            var m = CHAT_TOPIC.matcher(dest);
            if (m.matches()) {
              UUID roomId = UUID.fromString(m.group(1));
              UUID userId = extractUserId(acc.getUser());
              if (userId == null) {
                throw new org.springframework.messaging.MessagingException("Unauthenticated");
              }
              // ⬇️ 여기서 런타임에 레포지토리를 꺼냄 (JPA 의존을 Config 시점에 물지 않음)
              ChatMembershipRepository repo = ctx.getBean(ChatMembershipRepository.class);
              if (!repo.isMember(roomId, userId)) {
                throw new org.springframework.messaging.MessagingException("Not a member");
              }
            }
          }
        }
        return message;
      }

      private UUID extractUserId(java.security.Principal principal) {
        if (principal instanceof Authentication auth
            && auth.getPrincipal() instanceof CustomUserDetails cud) {
          return cud.getUser().getId();
        }
        return null;
      }
    };

    // 등록 순서: JWT → 구독 권한
    registration.interceptors(jwtConnect, subscribeAuth);
  }
}
