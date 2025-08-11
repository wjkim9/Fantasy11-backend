package likelion.mlb.backendProject.domain.player.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
    private UUID id;
    private Integer code;
    private Integer fplId;
    private String name;
    private String krName;
    private short win;
    private short draw;
    private short lose;
    private short points;
    private short played;
    private short position;
}
