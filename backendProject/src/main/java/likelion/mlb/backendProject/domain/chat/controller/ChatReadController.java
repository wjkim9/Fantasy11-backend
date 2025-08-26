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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
@Tag(name = "Chat Read State", description = "채팅 만음 상태 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
@lombok.extern.slf4j.Slf4j
public class ChatReadController {

  private final ChatReadService chatReadService;
  private final ChatMembershipRepository membershipRepository;

  @Operation(summary = "릶음 상태 조회", description = "사용자의 마지막 읽은 메시지와 미읽 개수를 조회합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "읽음 상태 조회 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님"),
      @ApiResponse(responseCode = "500", description = "내부 서버 오류")
  })
  @GetMapping("/{roomId}/read-state")
  public ResponseEntity<?> getReadState(
      @Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId,
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

  @Operation(summary = "메시지 읽음 처리", description = "지정된 메시지까지 읽음으로 표시합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 메시지 ID"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님"),
      @ApiResponse(responseCode = "500", description = "내부 서버 오류")
  })
  @PostMapping("/{roomId}/read-state")
  public ResponseEntity<?> markRead(
      @Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId,
      @Parameter(description = "읽음 요청 데이터 (messageId 포함)") @RequestBody(required = false) Map<String, String> body,
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
