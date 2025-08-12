package likelion.mlb.backendProject.global.staticdata.dto.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FplElement {

    //fpl에 등록된 선수의 코드 -> opta_code는 p + code ex) p150242
    private Integer code;

    @JsonProperty("id")
    private Integer fplId;

    @JsonProperty("element_type")
    private Integer elementType;

    @JsonProperty("web_name")
    private String webName;

    //다음 라운드에 뛸 수 있는지
    @JsonProperty("chance_of_playing_next_round")
    private boolean chanceOfPlayingNextRound;

    //해당 라운드에 뛸 수 있는지
    @JsonProperty("chance_of_playing_this_round")
    private boolean chanceOfPlayingThisRound;

    //fpl 점수 산정 기준 점수
    @JsonProperty("event_points")
    private Integer eventPoints;

    //해당 라운드에 해당 선수에 대한 소식 (없으면 빈 문자열)
    private String news;

    //해당 라운드 출전 가능 여부 ("a"는 가능)
    private String status;

    @JsonProperty("team_code")
    private Integer teamCode;

    //fpl에서 선수 가치를 기준으로 코스트를 선정
    @JsonProperty("now_cost")
    private short cost;

    private Integer team;
}
