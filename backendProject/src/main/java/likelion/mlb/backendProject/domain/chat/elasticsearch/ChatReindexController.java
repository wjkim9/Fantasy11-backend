package likelion.mlb.backendProject.domain.chat.elasticsearch;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Chat Search Management", description = "채팅 검색 인덱스 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatReindexController {
  private final ChatMessageRepository repo;
  private final ChatSearchIndexer indexer;

  @Operation(summary = "채팅 메시지 재인덱싱", description = "지정된 채팅방의 모든 메시지를 Elasticsearch에 재인덱싱합니다")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "재인덱싱 성공"),
      @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음"),
      @ApiResponse(responseCode = "500", description = "내부 서버 오류")
  })
  @PostMapping("/{roomId}/reindex")
  public void reindex(@Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId) {
    // 대용량이면 Page로 분할 처리
    repo.findAllByChatRoomIdOrderByCreatedAtAsc(roomId).forEach(indexer::index);
  }
}
