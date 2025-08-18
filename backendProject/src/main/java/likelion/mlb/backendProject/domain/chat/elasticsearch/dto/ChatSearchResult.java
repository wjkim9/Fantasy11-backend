package likelion.mlb.backendProject.domain.chat.elasticsearch.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatSearchResult {

  private final List<Item> items;    // 최신순 결과
  private final String nextCursor;   // 다음 페이지용 (없으면 null)
  private final boolean hasMore;

  @Getter
  @Builder
  public static class Item {

    private final UUID id;
    private final UUID chatRoomId;
    private final String content;
    private final UUID userId;
    private final Instant createdAt;
    private final String type;
  }
}