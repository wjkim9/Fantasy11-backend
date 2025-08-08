package likelion.mlb.backendProject.global.staticdata.dto.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FplEvent {

    @JsonProperty("id")
    private Integer fplId;
    // ex) Gameweek 1
    private String name;

    private boolean finished;

    //fpl 점수 기준 해당 라운드에서 가장 점수를 많이 낸 선수의 id
    @JsonProperty("top_element")
    private Integer topElement;

    //fpl 기준 해당 라운드에서 가장 많이 뽑힌 선수의 id
    @JsonProperty("most_transferred_in")
    private Integer mostTransferredIn;

    //“직전 라운드(바로 이전 라운드)” 라면 true
    @JsonProperty("is_previous")
    private boolean isPrevious;
    //“현재 진행 중인 라운드” 라면 true
    @JsonProperty("is_current")
    private boolean isCurrent;
    //“다음으로 진행될 라운드” 라면 true
    @JsonProperty("is_next")
    private boolean isNext;


}
