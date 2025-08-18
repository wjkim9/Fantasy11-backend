/*
package likelion.mlb.backendProject.global.configuration;

import java.security.Principal;
import java.util.UUID;
import java.util.regex.*;
import likelion.mlb.backendProject.domain.chat.repository.ChatMembershipRepository;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompSubscribeAuthInterceptor implements ChannelInterceptor {

  private static final Pattern CHAT_TOPIC = Pattern.compile("^/topic/chat/([0-9a-fA-F-]{36})$");
  private final ChatMembershipRepository membershipRepository;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      String dest = accessor.getDestination();
      if (dest != null) {
        var m = CHAT_TOPIC.matcher(dest);
        if (m.matches()) {
          UUID roomId = UUID.fromString(m.group(1));
          UUID userId = extractUserId(accessor.getUser());
          if (userId != null && !membershipRepository.isMember(roomId, userId)) {
            throw new AccessDeniedException("Not a member of this chat room");
          }
        }
      }
    }
    return message;
  }

  private UUID extractUserId(Principal principal) {
    if (principal instanceof Authentication auth
        && auth.getPrincipal() instanceof CustomUserDetails cud) {
      return cud.getUser().getId();
    }
    return null;
  }

  static class AccessDeniedException extends MessagingException {
    AccessDeniedException(String msg) { super(msg); }
  }
}*/
