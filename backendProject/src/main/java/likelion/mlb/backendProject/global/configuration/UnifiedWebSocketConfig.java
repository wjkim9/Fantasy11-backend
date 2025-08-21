package likelion.mlb.backendProject.global.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import likelion.mlb.backendProject.domain.chat.repository.ChatMembershipRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.domain.user.entity.User;
import likelion.mlb.backendProject.domain.user.repository.UserRepository;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import likelion.mlb.backendProject.global.security.jwt.JwtTokenProvider;

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
import org.springframework.messaging.support.MessageHeaderAccessor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class UnifiedWebSocketConfig implements WebSocketMessageBrokerConfigurer, ApplicationContextAware {

  private final ApplicationContext ctx;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    // do nothing (we already have ctx via ctor)
  }

  /* ===================== Endpoints ===================== */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 1) 채팅용 엔드포인트 (/ws) — 핸드셰이크에서 엔드포인트 플래그만 심음
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .addInterceptors(new EndpointFlagInterceptor("/ws"))
        .withSockJS();

    // 2) 드래프트용 엔드포인트 (/ws-draft) — 핸드셰이크에서 JWT 검증 + Principal 주입 + 엔드포인트 플래그
    registry.addEndpoint("/ws-draft")
        .setAllowedOriginPatterns("*")
        .addInterceptors(new DraftHandshakeInterceptor(ctx))
        .setHandshakeHandler(new DefaultHandshakeHandler() {
          @Override
          protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
            return (Principal) attributes.get("principal"); // DraftHandshakeInterceptor가 넣어둔 Authentication
          }
        })
        .withSockJS();
  }

  /* ===================== Broker ===================== */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setUserDestinationPrefix("/user");
  }

  /* ===================== Inbound Interceptor ===================== */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {

      // 패턴들
      private final Pattern CHAT_TOPIC   = Pattern.compile("^/topic/chat/([0-9a-fA-F\\-]{36})$");
      private final Pattern DRAFT_TOPIC  = Pattern.compile("^/topic/draft\\.([0-9a-fA-F\\-]{36})$");
      private final Pattern CHAT_SEND    = Pattern.compile("^/app/chat/([0-9a-fA-F\\-]{36})/send$");

      // 지연 빈 조회
      private ParticipantRepository participantRepo;
      private ChatMembershipRepository chatMembershipRepo;
      private DraftRepository draftRepo;
      private JwtTokenProvider jwtTokenProvider;
      private UserRepository userRepository;
      private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

      private void ensureBeans() {
        if (participantRepo == null) {
          participantRepo    = ctx.getBean(ParticipantRepository.class);
          chatMembershipRepo = ctx.getBean(ChatMembershipRepository.class);
          draftRepo          = ctx.getBean(DraftRepository.class);
          jwtTokenProvider   = ctx.getBean(JwtTokenProvider.class);
          userRepository     = ctx.getBean(UserRepository.class);
          objectMapper       = ctx.getBean(com.fasterxml.jackson.databind.ObjectMapper.class);
        }
      }

      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        ensureBeans();
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) return message;

        final String endpoint = getEndpointFlag(acc); // "/ws" or "/ws-draft" or null

        /* 0) Principal 복구 시도 (세션 → STOMP Native Header → 쿼리스트링) */
        if (acc.getUser() == null) {
          // (a) 핸드셰이크에서 심어둔 Authentication
          Object userAttr = acc.getSessionAttributes() != null ? acc.getSessionAttributes().get("user") : null;
          if (userAttr instanceof Authentication auth) {
            acc.setUser(auth);
          } else {
            // (b) STOMP Native Header: Authorization / authorization
            String authz = acc.getFirstNativeHeader("Authorization");
            if (authz == null) authz = acc.getFirstNativeHeader("authorization");
            String token = (authz != null && authz.startsWith("Bearer ")) ? authz.substring(7) : null;

            // (c) 쿼리파라미터 fallback (?token= 또는 ?access_token=)
            if (token == null && acc.getSessionAttributes() != null) {
              String qs = (String) acc.getSessionAttributes().get("queryString");
              token = extractTokenFromQuery(qs);
            }

            if (token != null && jwtTokenProvider.validateToken(token)) {
              Authentication auth = jwtTokenProvider.getAuthentication(token);
              acc.setUser(auth);
            }
          }
        }

        /* 1) CONNECT — /ws-draft 에서는 인증 강제, /ws는 느슨(구독/발행 단계에서 권한 검사) */
        if (StompCommand.CONNECT.equals(acc.getCommand())) {
          if ("/ws-draft".equals(endpoint) && acc.getUser() == null) {
            throw new AccessDeniedException("Unauthenticated (ws-draft)");
          }
          return message;
        }

        /* 2) SUBSCRIBE 권한 체크 */
        if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
          String dest = acc.getDestination();
          if (dest == null) return message;

          Authentication auth = (Authentication) acc.getUser();
          UUID userId = getUserId(auth);

          // 채팅 방 구독: /topic/chat/{roomId}
          var m1 = CHAT_TOPIC.matcher(dest);
          if (m1.matches()) {
            if (userId == null) throw new AccessDeniedException("Unauthenticated");
            UUID roomId = UUID.fromString(m1.group(1));
            if (!chatMembershipRepo.isMember(roomId, userId)) {
              throw new AccessDeniedException("Not a member of chat room");
            }
          }

          // 드래프트 방 구독: /topic/draft.{draftId}
          var m2 = DRAFT_TOPIC.matcher(dest);
          if (m2.matches()) {
            if (userId == null) throw new AccessDeniedException("Unauthenticated");
            UUID draftId = UUID.fromString(m2.group(1));
            if (!participantRepo.existsByDraft_IdAndUser_Id(draftId, userId)) {
              throw new AccessDeniedException("No permission for this draft");
            }
          }
          return message;
        }

        /* 3) SEND 권한 체크 */
        if (StompCommand.SEND.equals(acc.getCommand())) {
          String dest = acc.getDestination();
          Authentication auth = (Authentication) acc.getUser();
          UUID userId = getUserId(auth);
          if (userId == null) throw new AccessDeniedException("Unauthenticated");
          if (dest == null) return message;

          // 채팅 전송: /app/chat/{roomId}/send
          var mSendChat = CHAT_SEND.matcher(dest);
          if (mSendChat.matches()) {
            UUID roomId = UUID.fromString(mSendChat.group(1));
            if (!chatMembershipRepo.isMember(roomId, userId)) {
              throw new AccessDeniedException("No permission to send to this chat");
            }
            return message;
          }

          // 드래프트 선택: /app/draft/selectPlayer  (draftId는 헤더/바디에서 추출)
          if ("/app/draft/selectPlayer".equals(dest)) {
            UUID draftId = getDraftIdFromHeaderOrPayload(message, acc);
            if (!participantRepo.existsByDraft_IdAndUser_Id(draftId, userId)) {
              throw new AccessDeniedException("No permission to send to this draft");
            }
          }
        }

        return message;
      }

      /* ============ helpers ============ */

      private String getEndpointFlag(StompHeaderAccessor acc) {
        if (acc.getSessionAttributes() == null) return null;
        Object ep = acc.getSessionAttributes().get("wsEndpoint");
        return (ep instanceof String) ? (String) ep : null;
      }

      private UUID getUserId(Authentication auth) {
        if (auth == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof CustomUserDetails cud) return cud.getUser().getId();
        if (p instanceof User u) return u.getId();
        return null;
      }

      private String extractTokenFromQuery(String qs) {
        if (qs == null) return null;
        for (String pair : qs.split("&")) {
          if (pair.startsWith("access_token=") || pair.startsWith("token=")) {
            String val = pair.substring(pair.indexOf('=') + 1);
            String dec = URLDecoder.decode(val, StandardCharsets.UTF_8);
            if (dec.startsWith("Bearer ")) dec = dec.substring(7);
            return dec;
          }
        }
        return null;
      }

      private UUID getDraftIdFromHeaderOrPayload(Message<?> message, StompHeaderAccessor acc) {
        String h1 = acc.getFirstNativeHeader("X-DRAFT-ID");
        if (h1 != null && !h1.isBlank()) return UUID.fromString(h1);

        String h2 = acc.getFirstNativeHeader("X-ROOM-ID"); // 하위 호환
        if (h2 != null && !h2.isBlank()) return UUID.fromString(h2);

        Object payload = message.getPayload();
        if (payload instanceof byte[] body) {
          try {
            var node = objectMapper.readTree(body);
            String did = node.hasNonNull("draftId") ? node.get("draftId").asText(null) : null;
            if (did != null) return UUID.fromString(did);
            String rid = node.hasNonNull("roomId") ? node.get("roomId").asText(null) : null;
            if (rid != null) return UUID.fromString(rid);
          } catch (Exception ignore) {}
        }
        throw new AccessDeniedException("draftId header/body required");
      }
    });
  }

  /* ===================== Handshake interceptors ===================== */

  /** /ws 에 붙는 단순 플래그 인터셉터 */
  static class EndpointFlagInterceptor implements HandshakeInterceptor {
    private final String flag;
    EndpointFlagInterceptor(String flag) { this.flag = flag; }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Map<String, Object> attributes) {
      attributes.put("wsEndpoint", flag);
      // 쿼리스트링 저장(토큰 fallback용)
      String qs = request.getURI().getQuery();
      if (qs != null) attributes.put("queryString", qs);
      return true;
    }
    @Override public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
  }

  /** /ws-draft 에 붙는 JWT 검증 + Principal 주입 인터셉터 */
  @RequiredArgsConstructor
  static class DraftHandshakeInterceptor implements HandshakeInterceptor {
    private final ApplicationContext ctx;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Map<String, Object> attributes) {
      attributes.put("wsEndpoint", "/ws-draft");
      String qs = request.getURI().getQuery();
      if (qs != null) attributes.put("queryString", qs);

      String token = null;
      if (qs != null) {
        for (String pair : qs.split("&")) {
          if (pair.startsWith("token=") || pair.startsWith("access_token=")) {
            token = URLDecoder.decode(pair.substring(pair.indexOf('=') + 1), StandardCharsets.UTF_8);
            break;
          }
        }
      }
      if (token != null && token.startsWith("Bearer ")) token = token.substring(7);

      JwtTokenProvider jwt = ctx.getBean(JwtTokenProvider.class);
      if (token == null || !jwt.validateToken(token)) {
        throw new AccessDeniedException("Invalid or missing token");
      }

      String email = jwt.getEmail(token);
      UserRepository userRepo = ctx.getBean(UserRepository.class);
      var user = userRepo.findByEmail(email).orElseThrow(() -> new AccessDeniedException("User not found: " + email));

      var cud = new CustomUserDetails(user);
      Authentication auth = new UsernamePasswordAuthenticationToken(cud, null, cud.getAuthorities());

      attributes.put("user", auth);
      attributes.put("principal", auth);
      return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
        WebSocketHandler wsHandler, Exception exception) {}
  }
}
