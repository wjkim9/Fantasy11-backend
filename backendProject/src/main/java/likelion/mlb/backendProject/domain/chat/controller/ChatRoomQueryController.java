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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Chat Room", description = "채팅방 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatRoomQueryController {

  private final ChatRoomQueryService queryService;
  private final ChatRoomService chatRoomService;
  private final ChatRoomRepository chatRoomRepository;

  @Operation(summary = "채팅방 스코어보드 조회", description = "채팅방 참가자들의 점수와 순위를 조회합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "스코어보드 조회 성공"),
      @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
  })
  @GetMapping("/{roomId}/scoreboard")
  public List<ScoreboardItem> scoreboard(@Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId) {
    return queryService.getScoreboard(roomId);
  }

  @Operation(summary = "참가자 로스터 조회", description = "특정 참가자의 11명 로스터와 포메이션 정보를 조회합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "로스터 조회 성공"),
      @ApiResponse(responseCode = "404", description = "참가자를 찾을 수 없음")
  })
  @GetMapping("/{roomId}/participants/{participantId}/roster")
  public RosterResponse roster(
      @Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId,
      @Parameter(description = "참가자 ID", required = true) @PathVariable UUID participantId) {
    return queryService.getRoster(roomId, participantId);
  }

  @Operation(summary = "드래프트 채팅방 조회", description = "드래프트 ID로 해당 채팅방을 조회합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "채팅방 조회 성공"),
      @ApiResponse(responseCode = "404", description = "드래프트에 해당하는 채팅방을 찾을 수 없음")
  })
  @GetMapping("/by-draft/{draftId}")
  public ChatRoom getChatRoomByDraft(@Parameter(description = "드래프트 ID", required = true) @PathVariable UUID draftId) {
    return chatRoomRepository.findByDraftId(draftId)
        .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found for draft " + draftId));
  }

  @Operation(summary = "채팅방 생성", description = "드래프트를 위한 새 채팅방을 생성합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "채팅방 생성 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
  })
  @PostMapping
  public ChatRoom createChatRoom(@Parameter(description = "채팅방 생성 요청") @RequestBody CreateChatRoomRequest request) {
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
