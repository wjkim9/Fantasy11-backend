  package likelion.mlb.backendProject.domain.chat.dto;

  import java.util.UUID;
  import lombok.AllArgsConstructor;
  import lombok.Getter;
  import lombok.NoArgsConstructor;
  import lombok.Setter;

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public class PresenceDto {
    private UUID roomId;
    private UUID userId;
    private boolean online;
    private long onlineCount;
  }