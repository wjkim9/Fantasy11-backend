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

    private String teamName; // 소속팀 영어명

    private String teamKrName; // 소속팀 한글명

    private String elementTypePluralName; // 포지션 영어명

    private String elementTypeKrName; // 포지션 한글명
}
