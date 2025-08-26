package likelion.mlb.backendProject.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@Builder
public class ChatRoomDto {
  private UUID roomId;
  private UUID draftId;
}