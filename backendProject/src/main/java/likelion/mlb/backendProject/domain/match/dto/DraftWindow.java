package likelion.mlb.backendProject.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class DraftWindow {
    private UUID roundId;
    private short roundNo;
    private LocalDateTime openAt;
    private LocalDateTime lockAt;
}