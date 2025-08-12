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
    private UUID id; // player pk값

    private String webName; // player 영어 이름

    private String krName; // player 한글 이름

    private String pic; // player 사진

    private String teamName; // 소속팀 영어명

    private String teamKrName; // 소속팀 한글명

    private String elementTypePluralName; // 포지션 영어명

    private String elementTypeKrName; // 포지션 한글명

    /**
     * PlayerDto 리스트 → PlayerEsDocument 리스트 변환
     */
    public static List<PlayerEsDocument> toDocumentList(List<PlayerDto> playerDtoList) {
        return playerDtoList.stream().map(p -> PlayerEsDocument.builder()
                .id(p.getId())
                .webName(p.getWebName())
                .krName(p.getKrName())
                .pic(p.getPic())

                // team관련 설정
                .teamName(p.getTeamName())
                .teamKrName(p.getTeamKrName())

                // 포지션(elementType) 관련 설정
                .elementTypePluralName(p.getElementTypePluralName())
                .elementTypeKrName(p.getElementTypeKrName())
                .build()
        ).collect(Collectors.toList());
    }

}