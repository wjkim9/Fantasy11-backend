package likelion.mlb.backendProject.domain.chat.controller;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.service.ChatPresenceService;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatPresenceRestController {

  private final ChatPresenceService presenceService;

  @PostMapping("/{roomId}/join")
  public ChatPresenceService.ChatPresenceResponse join(
      @PathVariable UUID roomId,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    return presenceService.join(roomId, principal.getUser().getId());
  }

  @PostMapping("/{roomId}/leave")
  public ChatPresenceService.ChatPresenceResponse leave(
      @PathVariable UUID roomId,
      @AuthenticationPrincipal CustomUserDetails principal
  ) {
    return presenceService.leave(roomId, principal.getUser().getId());
  }
}