package likelion.mlb.backendProject.global.staticdata.dto.bootstrap;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true) //나머지 필드는 무시
@Getter
public class BootstrapStatic {

    @JsonProperty("events")
    private List<FplEvent> events;
    @JsonProperty("teams")
    private List<FplTeam> fplTeams;
    @JsonProperty("elements")
    private List<FplElement> elements;

    @JsonProperty("element_types")
    private List<FplElementType> elementTypes;

//    @JsonProperty("game_settings")
//    private GameSettings gameSettings;


}
