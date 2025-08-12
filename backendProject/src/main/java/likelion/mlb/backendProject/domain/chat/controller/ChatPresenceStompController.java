package likelion.mlb.backendProject.domain.chat.controller;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.PresenceDto;
import likelion.mlb.backendProject.domain.chat.service.ChatPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatPresenceStompController {

  private final ChatPresenceService presenceService;

  @MessageMapping("/chat.join")
  public void join(@Payload PresenceDto dto, java.security.Principal principal) {
    UUID userId = extractUserId(principal);
    presenceService.join(dto.getRoomId(), userId);
  }

  @MessageMapping("/chat.leave")
  public void leave(@Payload PresenceDto dto, java.security.Principal principal) {
    UUID userId = extractUserId(principal);
    presenceService.leave(dto.getRoomId(), userId);
  }

  private UUID extractUserId(java.security.Principal principal) {
    if (principal instanceof org.springframework.security.core.Authentication auth) {
      var cud = (likelion.mlb.backendProject.global.security.dto.CustomUserDetails) auth.getPrincipal();
      return cud.getUser().getId();
    }
    throw new IllegalStateException("Unauthenticated STOMP principal");
  }
}