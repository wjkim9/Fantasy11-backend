package likelion.mlb.backendProject.domain.chat.dto;

import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScoreboardItem {
  private UUID participantId;
  private UUID userId;        // 봇이면 null
  private String email;       // 봇이면 "BOT"
  private long totalPoints;   // 라운드 퍼포먼스 합계
  private int rank;           // 1~4
}