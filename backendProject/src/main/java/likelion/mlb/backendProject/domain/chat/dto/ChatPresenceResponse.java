package likelion.mlb.backendProject.domain.chat.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
public class ChatPresenceResponse {
  public final UUID roomId;
  public final UUID userId;
  public final boolean joined;
  public final long onlineCount;

}