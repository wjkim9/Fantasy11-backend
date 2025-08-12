package likelion.mlb.backendProject.domain.chat.controller;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import likelion.mlb.backendProject.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRestController {

  private final ChatService chatService;

  /**
   * GET /api/chat/rooms/{roomId}/messages/previous
   * cursor: ISO-8601 포맷의 LocalDateTime (예: 2025-08-06T11:00:00)
   * size: 한 번에 가져올 메시지 개수
   */
  @GetMapping("/{roomId}/messages/previous")
  public List<ChatMessageDto> getPreviousMessages(
      @PathVariable UUID roomId,
      @RequestParam("cursor") String cursor,
      @RequestParam(defaultValue = "20") int size
  ) {
    LocalDateTime cursorTime = LocalDateTime.parse(cursor);
    return chatService.getPreviousMessages(roomId, cursorTime, size);
  }
}
