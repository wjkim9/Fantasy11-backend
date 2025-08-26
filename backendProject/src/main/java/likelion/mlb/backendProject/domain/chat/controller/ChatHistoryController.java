package likelion.mlb.backendProject.domain.chat.controller;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatHistoryPage;
import likelion.mlb.backendProject.domain.chat.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Chat History", description = "채팅 히스토리 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatHistoryController {

  private final ChatHistoryService chatHistoryService;

  @Operation(summary = "최신 채팅 메시지 조회", description = "채팅방의 최신 메시지들을 지정된 개수만큼 조회합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "메시지 조회 성공"),
      @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
  })
  @GetMapping("/{roomId}/messages")
  public ResponseEntity<ChatHistoryPage> latest(
      @Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId,
      @Parameter(description = "조회할 메시지 개수 (기본값: 30)") @RequestParam(defaultValue = "30") int limit) {
    return ResponseEntity.ok(chatHistoryService.loadLatest(roomId, limit));
  }

  @Operation(summary = "이전 채팅 메시지 조회", description = "커서 기반으로 이전 메시지들을 조회합니다 (무한스크롤용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "메시지 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 커서 값"),
      @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
  })
  @GetMapping("/{roomId}/messages/before")
  public ResponseEntity<ChatHistoryPage> before(
      @Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId,
      @Parameter(description = "페이지 커서 (이전 조회의 nextCursor 값)", required = true) @RequestParam String cursor,
      @Parameter(description = "조회할 메시지 개수 (기본값: 30)") @RequestParam(defaultValue = "30") int limit) {
    return ResponseEntity.ok(chatHistoryService.loadBefore(roomId, cursor, limit));
  }
}