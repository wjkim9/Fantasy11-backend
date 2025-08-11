package likelion.mlb.backendProject.domain.player.cache.dto;

import likelion.mlb.backendProject.domain.player.elasticsearch.dto.PlayerEsDocument;
import likelion.mlb.backendProject.domain.player.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDto {
    private UUID id;
    private Integer code;
    private String webName;
    private String krName;
    private String pic;
    private boolean chanceOfPlayingNextRound;
    private boolean chanceOfPlayingThisRound;
    private short cost;
    private String status;
    private String news;
    private Integer teamCode;
    private Integer etId;

    private TeamDto team;
    private ElementTypeDto elementType;

    /**
     * PlayerDto 리스트 → PlayerEsDocument 리스트 변환
     */
    public static List<PlayerEsDocument> toDocumentList(List<PlayerDto> playerDtoList) {
        return playerDtoList.stream().map(p -> PlayerEsDocument.builder()
                .id(p.getId())
//                .code(p.getCode())
                .webName(p.getWebName())
                .krName(p.getKrName())
                .pic(p.getPic())
                /*
                .chanceOfPlayingNextRound(p.isChanceOfPlayingNextRound())
                .chanceOfPlayingThisRound(p.isChanceOfPlayingThisRound())
                .cost(p.getCost())
                .status(p.getStatus())
                .news(p.getNews())
                .teamCode(p.getTeamCode())
                .etId(p.getEtId())
                 */

                // team관련 설정
                /*
                .teamId(p.getTeam().getId())
//                .code(p.getTeam().getCode())
                .teamFplId(p.getTeam().getFplId())
                */
                .teamName(p.getTeam().getName())
                .teamKrName(p.getTeam().getKrName())
                /*
                .teamWin(p.getTeam().getWin())
                .teamDraw(p.getTeam().getDraw())
                .teamLose(p.getTeam().getLose())
                .teamPoints(p.getTeam().getPoints())
                .teamPlayed(p.getTeam().getPlayed())
                .teamPosition(p.getTeam().getPosition())
                */
                // 포지션(elementType) 관련 설정
                /*
                .elementTypeId(p.getElementType().getId())
                .elementTypeFplId(p.getElementType().getFplId())*/
                .elementTypePluralName(p.getElementType().getPluralName())
                .elementTypeKrName(p.getElementType().getKrName())
                /*
                .elementTypeSquadMinPlay(p.getElementType().getSquadMinPlay())
                .elementTypeSquadMaxPlay(p.getElementType().getSquadMaxPlay())
                .elementTypeElementCount(p.getElementType().getElementCount())
                */
                .build()
        ).collect(Collectors.toList());
    }

}