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

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "player-index") // 인덱스명
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEsDocument {

    @Id
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
}
