package likelion.mlb.backendProject.global.staticdata.dto.live;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class LiveFixtureDto {

    @JsonProperty("id")
    private Integer fixtureId;
    private Integer event;
    private Boolean finished;
    private Boolean started;

    private Integer minutes;

    @JsonProperty("team_h")
    private Integer homeTeamId;
    @JsonProperty("team_h_score")
    private Integer homeScore;

    @JsonProperty("team_a")
    private Integer awayTeamId;
    @JsonProperty("team_a_score")
    private Integer awayScore;

    @JsonProperty("team_h_difficulty")
    private Integer homeDifficulty;
    @JsonProperty("team_a_difficulty")
    private Integer awayDifficulty;
}
