package likelion.mlb.backendProject.global.configuration;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.draft.handler.CustomHandshakeHandler;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import org.springframework.beans.BeansException;
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
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app", "/api");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws-chat")
        .setAllowedOriginPatterns("*")
        .withSockJS();

    registry.addEndpoint("/api/ws-draft")
        .setHandshakeHandler(new CustomHandshakeHandler())
        .setAllowedOriginPatterns("*");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {

      private ParticipantRepository participantRepo;
      private ChatRoomRepository chatRoomRepo;

      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
          String dest = accessor.getDestination();
          if (dest != null && dest.matches("/topic/chat/[0-9a-fA-F\\-]{36}")) {
            // lazy-init repository beans
            if (participantRepo == null || chatRoomRepo == null) {
              this.participantRepo = applicationContext.getBean(ParticipantRepository.class);
              this.chatRoomRepo    = applicationContext.getBean(ChatRoomRepository   .class);
            }

            UUID roomId = UUID.fromString(dest.substring("/topic/chat/".length()));
            Principal user = accessor.getUser();
            UUID draftId = null;
            try {
              draftId = chatRoomRepo.findById(roomId)
                  .orElseThrow(() -> new AccessDeniedException("존재하지 않는 방입니다."))
                  .getDraft().getId();
            } catch (AccessDeniedException e) {
              throw new RuntimeException(e);
            }

            boolean allowed = participantRepo
                .existsByDraft_IdAndUser_Id(
                    draftId,
                    UUID.fromString(user.getName())
                );

            if (!allowed) {
              throw new RuntimeException(
                  new AccessDeniedException("이 방을 구독할 권한이 없습니다.")
              );
            }
          }
        }

        return message;
      }
    });
  }
}
