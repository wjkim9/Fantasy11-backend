package likelion.mlb.backendProject.domain.chat.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import likelion.mlb.backendProject.domain.chat.dto.ScoreboardItem;
import likelion.mlb.backendProject.domain.chat.dto.RosterResponse;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomQueryService;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatRoomQueryController {

  private final ChatRoomQueryService queryService;
  private final ChatRoomService chatRoomService;
  private final ChatRoomRepository chatRoomRepository;

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

  /**
   * 드래프트로 채팅방 조회
   */
  @GetMapping("/by-draft/{draftId}")
  public ChatRoom getChatRoomByDraft(@PathVariable UUID draftId) {
    return chatRoomRepository.findByDraftId(draftId)
        .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found for draft " + draftId));
  }

  /**
   * 채팅방 생성 (드래프트용)
   */
  @PostMapping
  public ChatRoom createChatRoom(@RequestBody CreateChatRoomRequest request) {
    return chatRoomService.createForDraft(request.getDraftId());
  }

  /**
   * 채팅방 생성 요청 DTO
   */
  public static class CreateChatRoomRequest {
    private UUID draftId;
    
    public UUID getDraftId() { return draftId; }
    public void setDraftId(UUID draftId) { this.draftId = draftId; }
  }
}
