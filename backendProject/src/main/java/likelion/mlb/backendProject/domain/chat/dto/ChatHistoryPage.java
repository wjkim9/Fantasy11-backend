package likelion.mlb.backendProject.domain.chat.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter @Builder
public class ChatHistoryPage {
  private final List<Item> items;      // 반환은 시간 오름차순(UI 바로 바인딩)
  private final String nextCursor;     // 더 이전 페이지용 커서 (없으면 null)
  private final boolean hasMore;

  @Getter @Builder
  public static class Item {
    private final UUID id;
    private final UUID chatRoomId;
    private final String type;         // USER | SYSTEM | ALERT
    private final String content;
    private final UUID userId;         // 시스템/봇이면 null
    private final Instant createdAt;
  }
}