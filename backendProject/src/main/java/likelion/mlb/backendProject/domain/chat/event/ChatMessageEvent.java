package likelion.mlb.backendProject.domain.chat.event;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageEvent {

  private UUID messageId;
  private UUID roomId;
  private UUID userId;
  private String content;
  private String messageType;
  private LocalDateTime createdAt;

  public static ChatMessageEvent fromDto(
      UUID msgId,
      UUID roomId,
      UUID userId,
      String content,
      String type,
      LocalDateTime time
  ) {
    return new ChatMessageEvent(msgId, roomId, userId, content, type, time);
  }

}
