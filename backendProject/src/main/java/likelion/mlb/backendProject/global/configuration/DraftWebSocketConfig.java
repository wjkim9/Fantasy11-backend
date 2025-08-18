package likelion.mlb.backendProject.global.configuration;

import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;
import likelion.mlb.backendProject.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class DraftWebSocketConfig implements WebSocketMessageBrokerConfigurer, ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Draft Endpoint
    registry.addEndpoint("/ws-draft")
            .setAllowedOriginPatterns("*")
            .addInterceptors(new HandshakeInterceptor() {
              @Override
              public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                             WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                String query = request.getURI().getQuery();
                String token = null;
                if (query != null) {
                  String[] params = query.split("&");
                  for (String param : params) {
                    if (param.startsWith("token=")) {
                      token = param.substring(6);
                      try {
                        token = java.net.URLDecoder.decode(token, "UTF-8");
                      } catch (Exception ignored) {}
                      break;
                    }
                  }
                }

                if (token != null && token.startsWith("Bearer ")) {
                  token = token.substring(7);
                }

                if (token != null && applicationContext.getBean(JwtTokenProvider.class).validateToken(token)) {
                  String email = applicationContext.getBean(JwtTokenProvider.class).getEmail(token);
                  var user = applicationContext.getBean(UserRepository.class)
                          .findByEmail(email)
                          .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

                  var cud = new likelion.mlb.backendProject.global.security.dto.CustomUserDetails(user);
                  var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                          cud, null, cud.getAuthorities()
                  );

                  // 세션에 Principal 저장
                  attributes.put("user", auth);
                  attributes.put("principal", auth);

                } else {
                  throw new RuntimeException(new java.nio.file.AccessDeniedException("Invalid or missing token"));
                }

                return true;
              }

              @Override
              public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                         WebSocketHandler wsHandler, Exception exception) {}
            })
            // ✅ (옵션 A) Principal을 세션 속성이 아닌 실제 Principal로 등록
            .setHandshakeHandler(new DefaultHandshakeHandler() {
              @Override
              protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                                Map<String, Object> attributes) {
                // beforeHandshake에서 저장한 Authentication 꺼내서 Principal로 리턴
                return (Principal) attributes.get("principal");
              }
            })
            .withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {

      private ParticipantRepository participantRepo;
      private ChatRoomRepository chatRoomRepo;
      private DraftRepository draftRepo;
      private JwtTokenProvider jwtTokenProvider;
      private UserRepository userRepository;
      private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

      private void ensureBeans() {
        if (participantRepo == null) {
          participantRepo   = applicationContext.getBean(ParticipantRepository.class);
          chatRoomRepo      = applicationContext.getBean(ChatRoomRepository.class);
          draftRepo         = applicationContext.getBean(DraftRepository.class);
          jwtTokenProvider  = applicationContext.getBean(JwtTokenProvider.class);
          userRepository    = applicationContext.getBean(UserRepository.class);
          objectMapper      = applicationContext.getBean(com.fasterxml.jackson.databind.ObjectMapper.class);
        }
      }

      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        ensureBeans();
        var accessor = StompHeaderAccessor.wrap(message);

        // 현재 메시지의 Principal이 없으면 세션에서 꺼내서 복원
        if (accessor.getUser() == null) {
          Object userAttr = accessor.getSessionAttributes().get("user");

          // 세션에 저장된 객체가 Authentication 타입이면 Principal로 설정
          if (userAttr instanceof Authentication auth) {
            accessor.setUser(auth);
          }
        }

        // 1. CONNECT: User Authentication
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          Object userAttr = accessor.getSessionAttributes().get("user");
          if (userAttr instanceof Authentication auth) {
            accessor.setUser(auth);
          } else {
            throw new RuntimeException(new AccessDeniedException("Unauthenticated"));
          }
        }

        // 2. SUBSCRIBE: Authorization for Topic
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
          var auth = (org.springframework.security.core.Authentication) accessor.getUser();
          if (auth == null) throw new RuntimeException(new java.nio.file.AccessDeniedException("Unauthenticated"));
          var cud    = (likelion.mlb.backendProject.global.security.dto.CustomUserDetails) auth.getPrincipal();
          var userId = cud.getUser().getId();
          String dest = accessor.getDestination();
          if (dest == null) {
            throw new RuntimeException(new java.nio.file.AccessDeniedException("Destination required"));
          }

          // Draft Subscription
          if (dest != null && dest.matches("^/topic/draft.[0-9a-fA-F\\-]{36}$")) {

            String draftIdStr = dest.substring("/topic/draft.".length());
            var draftId = java.util.UUID.fromString(draftIdStr);
            if (!participantRepo.existsByDraft_IdAndUser_Id(draftId, userId)) {
              throw new RuntimeException(new java.nio.file.AccessDeniedException("No permission for this draft"));
            }
          }

        }

        // 3. SEND: Authorization for Message
        if (StompCommand.SEND.equals(accessor.getCommand())) {

          var auth = (org.springframework.security.core.Authentication) accessor.getUser();
          if (auth == null) throw new RuntimeException(new java.nio.file.AccessDeniedException("Unauthenticated"));
          var cud    = (likelion.mlb.backendProject.global.security.dto.CustomUserDetails) auth.getPrincipal();
          var userId = cud.getUser().getId();
          String dest = accessor.getDestination();
          if (dest == null) {
            throw new RuntimeException(new java.nio.file.AccessDeniedException("Destination required"));
          }

          // Draft Message
          if ("/app/draft/selectPlayer".equals(dest)) {
            java.util.UUID draftId = getDraftIdFromPayloadOrHeader(message, accessor); // Assuming payload contains draftId as 'roomId'
            if (!participantRepo.existsByDraft_IdAndUser_Id(draftId, userId)) {
              throw new RuntimeException(new java.nio.file.AccessDeniedException("No permission to send message to this draft"));
            }
          }

        }
        return message;
      }

      private java.util.UUID getDraftIdFromPayloadOrHeader(Message<?> message, StompHeaderAccessor accessor) {
        // 1) 헤더 우선: X-DRAFT-ID 또는 X-ROOM-ID(하위호환)
        String h1 = accessor.getFirstNativeHeader("X-DRAFT-ID");
        if (h1 != null && !h1.isBlank()) return java.util.UUID.fromString(h1);

        String h2 = accessor.getFirstNativeHeader("X-ROOM-ID"); // 기존 호환
        if (h2 != null && !h2.isBlank()) return java.util.UUID.fromString(h2);

        // 2) 바디에서 찾기: draftId 우선, 없으면 roomId(하위호환)
        Object payload = message.getPayload();
        if (payload instanceof byte[] body) {
          try {
            var node = objectMapper.readTree(body);
            String did = node.hasNonNull("draftId") ? node.get("draftId").asText(null) : null;
            if (did != null) return java.util.UUID.fromString(did);
            String rid = node.hasNonNull("roomId") ? node.get("roomId").asText(null) : null;
            if (rid != null) return java.util.UUID.fromString(rid);
          } catch (Exception ignore) {}
        }

        throw new RuntimeException(new java.nio.file.AccessDeniedException("draftId header/body required"));
      }


    });
  }
}
