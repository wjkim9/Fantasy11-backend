package likelion.mlb.backendProject.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage.MessageType;
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

}
