package likelion.mlb.backendProject.domain.player.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ElementTypeDto {

    private UUID id;

    //fpl의 각 포지션별 id
    private Integer fplId;

    //ex) Goalkeepers, Defenders, fpl에서 받아오는 영어 이름
    private String pluralName;

    // 선수 포지션 한글명
    private String krName = "";

    // 베스트 일레븐에서 해당 포지션을 뽑아야 하는 최소 수
    private Integer squadMinPlay;

    // 베스트 일레븐에서 해당 포지션을 뽑아야 하는 최대 수
    private Integer squadMaxPlay;

}
