package likelion.mlb.backendProject.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MatchUuidResponse {
    private String userId;
    private long count;
}

