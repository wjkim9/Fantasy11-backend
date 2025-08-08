package likelion.mlb.backendProject.domain.team.dto;


import likelion.mlb.backendProject.domain.team.entity.Team;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TeamTableDto {

    private Integer fplId;
    private String name;
    private short win;
    private short draw;
    private short lose;
    private short points;
    private short played;
    private short position;

    public static List<TeamTableDto> toDto(List<Team> teams) {
        List<TeamTableDto> dtos = new ArrayList<>();

        for (Team team : teams) {
            TeamTableDto dto = new TeamTableDto();
            dto.fplId = team.getFplId();
            dto.name = team.getName();
            dto.win = team.getWin();
            dto.draw = team.getDraw();
            dto.lose = team.getLose();
            dto.points = team.getPoints();
            dto.played = team.getPlayed();
            dto.position = team.getPosition();
            dtos.add(dto);
        }
        return dtos;
    }
}
