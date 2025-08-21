package likelion.mlb.backendProject.domain.chat.controller;


import java.util.Map;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatReadStateDto;
import likelion.mlb.backendProject.domain.chat.repository.ChatMembershipRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatReadService;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
@lombok.extern.slf4j.Slf4j
public class ChatReadController {

  private final ChatReadService chatReadService;
  private final ChatMembershipRepository membershipRepository;

  @GetMapping("/{roomId}/read-state")
  public ResponseEntity<?> getReadState(@PathVariable UUID roomId,
      @AuthenticationPrincipal CustomUserDetails cud) {
    if (cud == null || cud.getUser() == null) {
      log.debug("[read-state] 401: no principal");
      return ResponseEntity.status(401).body(Map.of("error","UNAUTHORIZED"));
    }
    UUID userId = cud.getUser().getId();
    if (!membershipRepository.isMember(roomId, userId)) {
      log.debug("[read-state] 403: not a member. room={}, user={}", roomId, userId);
      return ResponseEntity.status(403).body(Map.of("error","FORBIDDEN"));
    }

    try {
      var last = chatReadService.getLastReadMessageId(roomId, userId).orElse(null);
      long unread = chatReadService.countUnread(roomId, userId);

      return ResponseEntity.ok(
          new ChatReadStateDto(last != null ? last.toString() : null, unread)
      );
    } catch (Exception e) {
      log.error("[read-state] fail. room={}, user={}", roomId, userId, e);
      return ResponseEntity.internalServerError().body(Map.of("error","INTERNAL_ERROR"));
    }
  }

  @PostMapping("/{roomId}/read-state")
  public ResponseEntity<?> markRead(@PathVariable UUID roomId,
      @RequestBody(required = false) Map<String, String> body,
      @AuthenticationPrincipal CustomUserDetails cud) {
    if (cud == null || cud.getUser() == null) {
      log.debug("[mark-read] 401: no principal");
      return ResponseEntity.status(401).body(Map.of("error","UNAUTHORIZED"));
    }
    UUID userId = cud.getUser().getId();
    if (!membershipRepository.isMember(roomId, userId)) {
      log.debug("[mark-read] 403: not a member. room={}, user={}", roomId, userId);
      return ResponseEntity.status(403).body(Map.of("error","FORBIDDEN"));
    }

    UUID messageId = null;
    if (body != null) {
      String mid = body.get("messageId");
      if (mid != null && !mid.isBlank()) {
        try { messageId = UUID.fromString(mid); }
        catch (IllegalArgumentException ex) {
          return ResponseEntity.badRequest().body(Map.of("error","INVALID_MESSAGE_ID"));
        }
      }
    }

    try {
      chatReadService.markReadUpTo(roomId, userId, messageId);
      long unread = chatReadService.countUnread(roomId, userId);
      return ResponseEntity.ok(Map.of("ok", true, "unreadCount", unread));
    } catch (Exception e) {
      log.error("[mark-read] fail. room={}, user={}, messageId={}", roomId, userId, messageId, e);
      return ResponseEntity.internalServerError().body(Map.of("error","INTERNAL_ERROR"));
    }
  }
}
