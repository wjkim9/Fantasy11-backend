package likelion.mlb.backendProject.domain.chat.elasticsearch;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
public class ChatReindexController {
  private final ChatMessageRepository repo;
  private final ChatSearchIndexer indexer;

  @PostMapping("/{roomId}/reindex")
  public void reindex(@PathVariable UUID roomId) {
    // 대용량이면 Page로 분할 처리
    repo.findAllByChatRoomIdOrderByCreatedAtAsc(roomId).forEach(indexer::index);
  }
}
