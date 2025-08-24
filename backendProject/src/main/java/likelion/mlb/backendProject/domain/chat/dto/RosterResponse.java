package likelion.mlb.backendProject.domain.chat.dto;

import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RosterResponse {

  private UUID participantId;
  private String formation; // "4-3-3"
  private List<PlayerSlot> players;

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class PlayerSlot {
    private UUID playerId;
    private String name;
    private String position;
    private String team;
    private int points;
    private String pic;
  }
}
