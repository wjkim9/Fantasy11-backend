package likelion.mlb.backendProject.domain.chat.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage.MessageType;
import likelion.mlb.backendProject.domain.chat.event.ChatMessageEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

  private UUID id;
  private UUID roomId;
  private UUID userId;
  private MessageType messageType;
  private String content;
  private LocalDateTime sentAt;

  // ① Kafka용 이벤트 → DTO
  public static ChatMessageDto from(ChatMessageEvent evt) {
    return new ChatMessageDto(
        evt.getMessageId(),
        evt.getRoomId(),
        evt.getUserId(),
        MessageType.valueOf(evt.getMessageType()),
        evt.getContent(),
        evt.getCreatedAt()
    );
  }



  public static ChatMessageDto from(ChatMessage msg) {
    return new ChatMessageDto(
        msg.getId(),
        msg.getChatRoom().getId(),
        msg.getUser() != null ? msg.getUser().getId() : null,
        msg.getMessageType(),
        msg.getContent(),
        msg.getCreatedAt()
    );
  }

}
