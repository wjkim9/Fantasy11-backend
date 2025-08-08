package likelion.mlb.backendProject.global.staticdata.dto.live;

import com.fasterxml.jackson.annotation.JsonProperty;
import likelion.mlb.backendProject.global.staticdata.dto.live.element.ExplainDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.element.StatsDto;
import lombok.Getter;

import java.util.List;

@Getter
public class LiveElementDto {
    // FPL player ID
    @JsonProperty("element")
    private Integer playerId;

    // 실시간 집계 스탯
    private StatsDto stats;

    // (옵션) 포인트 상세 설명
    private List<ExplainDto> explain;
}
