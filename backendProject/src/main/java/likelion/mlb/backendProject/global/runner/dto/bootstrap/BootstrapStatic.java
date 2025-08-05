package likelion.mlb.backendProject.global.runner.dto.bootstrap;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import likelion.mlb.backendProject.global.runner.dto.bootstrap.FplElement;
import likelion.mlb.backendProject.global.runner.dto.bootstrap.FplElementType;
import likelion.mlb.backendProject.global.runner.dto.bootstrap.FplEvent;
import likelion.mlb.backendProject.global.runner.dto.bootstrap.FplTeam;
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
