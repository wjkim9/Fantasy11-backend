package likelion.mlb.backendProject.domain.chat.controller;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatHistoryPage;
import likelion.mlb.backendProject.domain.chat.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatHistoryController {

  private final ChatHistoryService chatHistoryService;

  /** 최신 30개 */
  @GetMapping("/{roomId}/messages")
  public ResponseEntity<ChatHistoryPage> latest(
      @PathVariable UUID roomId,
      @RequestParam(defaultValue = "30") int limit) {
    return ResponseEntity.ok(chatHistoryService.loadLatest(roomId, limit));
  }

  /** 이전 페이지(커서 기반) */
  @GetMapping("/{roomId}/messages/before")
  public ResponseEntity<ChatHistoryPage> before(
      @PathVariable UUID roomId,
      @RequestParam String cursor,
      @RequestParam(defaultValue = "30") int limit) {
    return ResponseEntity.ok(chatHistoryService.loadBefore(roomId, cursor, limit));
  }
}