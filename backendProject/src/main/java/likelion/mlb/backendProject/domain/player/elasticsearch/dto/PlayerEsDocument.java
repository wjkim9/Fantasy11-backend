package likelion.mlb.backendProject.domain.player.elasticsearch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.UUID;

/**
* 엘라스틱서치에 적용 될 문서를 자바 객체로 정의한 클래스
 * 엘라스틱 전용 DTO
* */

// 이게 없으면 클래스명도 같이 Elasticsearch에 저장이 됨.
// 그렇게 되면 PlayerEsDocument와 매칭이 안 되어 search에 오류가 생김.
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "player-index") // 인덱스명
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEsDocument {

    @Id
    private UUID id;

//    private Integer code;
    private String webName;
    private String krName;
    private String pic;
    /*
    private boolean chanceOfPlayingNextRound;
    private boolean chanceOfPlayingThisRound;
    private short cost;
    private String status;
    private String news;
    private Integer teamCode;
    private Integer etId;
    */
    // 소속팀 관련 정보
//    private UUID teamId;
//    private Integer teamCode; // player엔티티의 teamCode와 겹쳐 우선 주석처리
//    private Integer teamFplId;

    private String teamName;
    private String teamKrName;

    /*
    private short teamWin;
    private short teamDraw;
    private short teamLose;
    private short teamPoints;
    private short teamPlayed;
    private short teamPosition;
    */

    // 포지션 관련 정보
    /*
    private UUID elementTypeId;
    private Integer elementTypeFplId;
     */
    private String elementTypePluralName;
    private String elementTypeKrName;
    /*
    private Integer elementTypeSquadMinPlay;
    private Integer elementTypeSquadMaxPlay;
    private Integer elementTypeElementCount;
    */
}
