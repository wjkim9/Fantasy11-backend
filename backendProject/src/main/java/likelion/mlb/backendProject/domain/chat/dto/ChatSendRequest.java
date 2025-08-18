package likelion.mlb.backendProject.domain.chat.dto;

import java.util.UUID;
import lombok.*;

@Getter @Setter
public class ChatSendRequest {
  private UUID roomId;
  private String content;
}