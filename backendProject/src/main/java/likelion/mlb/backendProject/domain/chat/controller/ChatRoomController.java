package likelion.mlb.backendProject.domain.chat.controller;

import likelion.mlb.backendProject.domain.chat.dto.ChatRoomDto;
import likelion.mlb.backendProject.domain.chat.dto.RosterResponse;
import likelion.mlb.backendProject.domain.chat.dto.ScoreboardItem;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomQueryService;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;

  /**
   * draftId를 통해 chatRoomId가져옴.
   */
  @GetMapping("/getChatroomId")
  public ChatRoomDto roster(@RequestParam UUID draftId) {
    UUID roomId = chatRoomService.getRoomIdByDraft(draftId);

    return ChatRoomDto.builder()
            .roomId(roomId)
            .draftId(draftId)
            .build();
  }
}
