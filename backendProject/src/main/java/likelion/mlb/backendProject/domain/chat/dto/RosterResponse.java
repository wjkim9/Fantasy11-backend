package likelion.mlb.backendProject.domain.chat.dto;

import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RosterResponse {

  private UUID participantId;
  private String formation; // 예: "4-3-3" (GK 제외, DF-MID-FWD)
  private List<PlayerSlot> players;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PlayerSlot {

    private UUID playerId;
    private String name;
    private String position; // GK/DEF/MID/FWD
    private String team;     // 팀 이름(옵션)
  }
}