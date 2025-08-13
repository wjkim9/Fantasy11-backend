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

    //a: 뛸 수 있음, d: 출전 불투명, i: 부상, s: 징계, u: 사용불(임대 등), n: 자격 없음(미등록 선수)
    //x로 업데이트 된 건 fpl api로부터 받아온 데이터에서 삭제된 선수
    private String status;

    private String teamName; // 소속팀 영어명

    private String teamKrName; // 소속팀 한글명

    private UUID elementTypeId; // 포지션 pk값

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
                .status(p.getStatus())

                // team관련 설정
                .teamName(p.getTeamName())
                .teamKrName(p.getTeamKrName())

                // 포지션(elementType) 관련 설정
                .elementTypeId(p.getElementTypeId())
                .elementTypePluralName(p.getElementTypePluralName())
                .elementTypeKrName(p.getElementTypeKrName())
                .build()
        ).collect(Collectors.toList());
    }

}