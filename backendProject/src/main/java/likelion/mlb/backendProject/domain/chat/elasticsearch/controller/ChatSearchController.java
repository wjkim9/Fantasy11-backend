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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Chat Search", description = "채팅 메시지 검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatSearchController {

  private final ChatSearchService chatSearchService;
  private final ChatMembershipRepository membershipRepository;

  @Operation(summary = "채팅 메시지 검색", description = "지정된 채팅방에서 키워드로 메시지를 검색합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "검색 성공"),
      @ApiResponse(responseCode = "403", description = "채팅방 멤버가 아님"),
      @ApiResponse(responseCode = "500", description = "내부 서버 오류")
  })
  @GetMapping("/{roomId}/search")
  public ResponseEntity<ChatSearchResult> search(
      @Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId,
      @Parameter(description = "검색 키워드") @RequestParam(required = false, defaultValue = "") String q,
      @Parameter(description = "페이지 커서 (이전 조회의 nextCursor 값)") @RequestParam(required = false) String cursor,
      @Parameter(description = "조회할 메시지 개수 (1-50, 기본값: 20)") @RequestParam(defaultValue = "20") int limit,
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
