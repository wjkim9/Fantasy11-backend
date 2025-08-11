package likelion.mlb.backendProject.global.configuration;

import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;
import likelion.mlb.backendProject.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app"); // STOMP는 /app 하나로 통일 권장
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-chat")
        .setAllowedOriginPatterns("*")
        .withSockJS();


    registry.addEndpoint("/api/ws-draft")
        .setAllowedOriginPatterns("*");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {

      // lazy bean refs
      private ParticipantRepository participantRepo;
      private ChatRoomRepository chatRoomRepo;
      private JwtTokenProvider jwtTokenProvider;
      private UserRepository userRepository;
      private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

      private void ensureBeans() {
        if (participantRepo == null) {
          participantRepo   = applicationContext.getBean(ParticipantRepository.class);
          chatRoomRepo      = applicationContext.getBean(ChatRoomRepository.class);
          jwtTokenProvider  = applicationContext.getBean(JwtTokenProvider.class);
          userRepository    = applicationContext.getBean(UserRepository.class);
          objectMapper      = applicationContext.getBean(com.fasterxml.jackson.databind.ObjectMapper.class);
        }
      }

      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        ensureBeans();
        var accessor = StompHeaderAccessor.wrap(message);
        if (accessor == null) return message;


        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          String raw = accessor.getFirstNativeHeader("Authorization"); // "Bearer xxx"
          if (raw != null && raw.startsWith("Bearer ")) raw = raw.substring(7);

          if (raw != null && jwtTokenProvider.validateToken(raw)) {
            String email = jwtTokenProvider.getEmail(raw); // 팀원과 합의대로 subject=email
            var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + email));

            var cud  = new likelion.mlb.backendProject.global.security.dto.CustomUserDetails(user);
            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                cud, null, cud.getAuthorities()
            );

            accessor.setUser(auth);
          }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
          String dest = accessor.getDestination();
          if (dest != null && dest.matches("^/topic/chat/[0-9a-fA-F\\-]{36}(/presence)?$")) {

            var auth = (org.springframework.security.core.Authentication) accessor.getUser();
            if (auth == null) throw new RuntimeException(new java.nio.file.AccessDeniedException("Unauthenticated"));

            var cud    = (likelion.mlb.backendProject.global.security.dto.CustomUserDetails) auth.getPrincipal();
            var userId = cud.getUser().getId();

            String idPart = dest.substring("/topic/chat/".length());
            if (idPart.endsWith("/presence")) idPart = idPart.substring(0, idPart.length() - "/presence".length());
            var roomId = java.util.UUID.fromString(idPart);

            var draftId = chatRoomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException(new java.nio.file.AccessDeniedException("존재하지 않는 방입니다.")))
                .getDraft().getId();

            boolean allowed = participantRepo.existsByDraft_IdAndUser_Id(draftId, userId);
            if (!allowed) {
              throw new RuntimeException(new java.nio.file.AccessDeniedException("이 방을 구독할 권한이 없습니다."));
            }
          }
        }


        if (StompCommand.SEND.equals(accessor.getCommand())) {
          String dest = accessor.getDestination();
          if ("/app/chat.send".equals(dest)) {
            var auth = (org.springframework.security.core.Authentication) accessor.getUser();
            if (auth == null) throw new RuntimeException(new java.nio.file.AccessDeniedException("Unauthenticated"));

            var cud    = (likelion.mlb.backendProject.global.security.dto.CustomUserDetails) auth.getPrincipal();
            var userId = cud.getUser().getId();

            java.util.UUID roomId = null;

            String headerRoomId = accessor.getFirstNativeHeader("X-ROOM-ID");
            if (headerRoomId != null) {
              roomId = java.util.UUID.fromString(headerRoomId);
            } else {
              Object payload = message.getPayload();
              if (payload instanceof byte[] body) {
                try {
                  var node = objectMapper.readTree(body);
                  var rid  = node.hasNonNull("roomId") ? node.get("roomId").asText(null) : null;
                  if (rid != null) roomId = java.util.UUID.fromString(rid);
                } catch (Exception ignore) { /* payload 파싱 실패 시 아래에서 에러 */ }
              }
            }

            if (roomId == null) {
              throw new RuntimeException(new java.nio.file.AccessDeniedException("roomId header/body required"));
            }

            var draftId = chatRoomRepo.findById(roomId)
                .orElseThrow(() -> new RuntimeException(new java.nio.file.AccessDeniedException("존재하지 않는 방입니다.")))
                .getDraft().getId();

            boolean allowed = participantRepo.existsByDraft_IdAndUser_Id(draftId, userId);
            if (!allowed) {
              throw new RuntimeException(new java.nio.file.AccessDeniedException("이 방에 전송 권한이 없습니다."));
            }
          }
        }


        return message;
      }
    });
  }
}

