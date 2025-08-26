package likelion.mlb.backendProject.domain.chat.dto;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreboardItem {

  private UUID participantId;
  private UUID userId;
  private String email;
  private int totalPoints;
  private int rank;
  private int leaguePoints;
}