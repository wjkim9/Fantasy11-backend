package likelion.mlb.backendProject.domain.chat.elasticsearch.controller;

import java.util.List;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.elasticsearch.dto.ChatSearchResult;
import likelion.mlb.backendProject.domain.chat.elasticsearch.service.ChatSearchService;
import likelion.mlb.backendProject.domain.chat.repository.ChatMembershipRepository;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatSearchController {

  private final ChatSearchService chatSearchService;
  private final ChatMembershipRepository membershipRepository;

  @GetMapping("/{roomId}/search")
  public ResponseEntity<ChatSearchResult> search(
      @PathVariable UUID roomId,
      @RequestParam(required = false, defaultValue = "") String q,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit,
      Authentication authentication) {

    // 권한 체크
    UUID userId = null;
    if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails cud) {
      userId = cud.getUser().getId();
    }
    int safeLimit = Math.max(1, Math.min(limit, 50));

    if (userId == null || !membershipRepository.isMember(roomId, userId)) {
      // 본문이 없는 403 대신, 항상 JSON을 주자 (프론트에서 r.json() 해도 안전)
      return ResponseEntity.status(403).body(ChatSearchResult.builder()
          .items(List.of()).hasMore(false).nextCursor(null).build());
    }

    ChatSearchResult result = chatSearchService.search(roomId, q, cursor, safeLimit);
    if (result == null) {
      result = ChatSearchResult.builder().items(List.of()).hasMore(false).nextCursor(null).build();
    }
    return ResponseEntity.ok(result);
  }
}
