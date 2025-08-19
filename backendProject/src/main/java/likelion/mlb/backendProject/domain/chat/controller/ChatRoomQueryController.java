package likelion.mlb.backendProject.domain.chat.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import likelion.mlb.backendProject.domain.chat.dto.ScoreboardItem;
import likelion.mlb.backendProject.domain.chat.dto.RosterResponse;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomQueryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatRoomQueryController {

  private final ChatRoomQueryService queryService;

  /**
   * 방 스코어보드 (4명, 점수/랭크)
   */
  @GetMapping("/{roomId}/scoreboard")
  public List<ScoreboardItem> scoreboard(@PathVariable UUID roomId) {
    return queryService.getScoreboard(roomId);
  }

  /**
   * 특정 참가자의 로스터(11인) + 포메이션
   */
  @GetMapping("/{roomId}/participants/{participantId}/roster")
  public RosterResponse roster(@PathVariable UUID roomId, @PathVariable UUID participantId) {
    return queryService.getRoster(roomId, participantId);
  }
}
