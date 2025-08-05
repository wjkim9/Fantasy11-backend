package likelion.mlb.backendProject.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MatchStatusResponse {
    private long count;
    private String state;
    private String remainingTime;
    private UUID roundId;
    private int roundNo;
    private String openAt;
    private String lockAt;
}

