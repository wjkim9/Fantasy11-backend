package likelion.mlb.backendProject.domain.match.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RoundInfo {
    private UUID id;
    private short no;
    private String openAt;
    private String lockAt;
}