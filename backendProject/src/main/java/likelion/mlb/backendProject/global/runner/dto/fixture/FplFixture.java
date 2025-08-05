package likelion.mlb.backendProject.global.runner.dto.fixture;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class FplFixture {

    private Integer code;

    private short event;

    @JsonProperty("id")
    private Integer fplId;

    private boolean started;
    private boolean finished;

    @JsonProperty("team_h")
    private Integer homeTeam;
    @JsonProperty("team_a")
    private Integer awayTeam;


    @JsonProperty("team_h_score")
    private Integer homeTeamScore;
    @JsonProperty("team_a_score")
    private Integer awayTeamScore;


    @JsonProperty("kickoff_time")
    private OffsetDateTime kickoffTime;

    private short minutes;
}
