package likelion.mlb.backendProject.domain.player.dto;

import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.entity.live.PlayerFixtureStat;
import likelion.mlb.backendProject.domain.team.entity.Team;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PreviousBestPlayerDto {

    //player 테이블에서 가져옴
    private Integer playerFplId;
    private String playerName;
    private String pic;

    //element_type 테이블에서 가져옴
    private String etName;

    //team 테이블에서 가져옴
    private String teamName;

    //PlayerFixtureStat 테이블에서 가져옴
    private Integer totalPoints;


    public static List<PreviousBestPlayerDto> toDto(List<PlayerFixtureStat> stats) {
        List<PreviousBestPlayerDto> dtos = new ArrayList<>();
        for (PlayerFixtureStat stat : stats) {
            Player player = stat.getPlayer();
            PreviousBestPlayerDto dto = new PreviousBestPlayerDto();
            dto.playerFplId = player.getFplId();
            dto.playerName = player.getWebName();
            dto.pic = player.getPic();
            dto.etName = player.getElementType().getPluralName();
            dto.teamName = player.getTeam().getName();
            dto.totalPoints = stat.getTotalPoints();
            dtos.add(dto);
        }
        return dtos;
    }

}
