package likelion.mlb.backendProject.global.staticdata.dto.live;

import likelion.mlb.backendProject.global.staticdata.dto.fixture.FplFixture;
import lombok.Getter;

import java.util.List;

@Getter
public class LiveEventDto {

    // 선수별 실시간 스탯
    private List<LiveElementDto> elements;

    // 경기별 진행 상태
    private List<FplFixture> fixtures;
}