package likelion.mlb.backendProject.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {

  private UUID id;
  private UUID DraftId;
  private LocalDateTime createdAt;
}
