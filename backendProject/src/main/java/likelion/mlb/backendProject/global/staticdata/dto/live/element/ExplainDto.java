package likelion.mlb.backendProject.global.staticdata.dto.live.element;

import lombok.Getter;

@Getter
public class ExplainDto {

    private Integer fixture;
    // 예: goals_scored, assists …
    private String identifier;
    // 해당 스탯 수치
    private Integer value;
    // 이 스탯이 기여한 포인트
    private Integer points;
}
