package likelion.mlb.backendProject.global.staticdata.dto.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FplElementType {

    //fpl의 각 포지션별 id
    private Integer id;

    //ex) Goalkeepers, Defenders
    @JsonProperty("plural_name")
    private String pluralName;

    //베스트 일레븐에서 해당 포지션을 뽑아야 하는 최소 수
    @JsonProperty("squad_min_play")
    private Integer squadMinPlay;

    //베스트 일레븐에서 해당 포지션을 뽑을 수 있는 최대 수
    @JsonProperty("squad_max_play")
    private Integer squadMaxPlay;

    //fpl에 해당 포지션으로 등록된 선수의 수
    @JsonProperty("element_count")
    private Integer elementCount;

}
