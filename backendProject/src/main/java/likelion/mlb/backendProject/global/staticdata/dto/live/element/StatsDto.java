package likelion.mlb.backendProject.global.staticdata.dto.live.element;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;


@Getter
public class StatsDto {

    private Integer minutes;
    @JsonProperty("goals_scored")        private Integer goalsScored;
    private Integer assists;
    @JsonProperty("clean_sheets")        private Integer cleanSheets;
    @JsonProperty("goals_conceded")      private Integer goalsConceded;
    @JsonProperty("own_goals")           private Integer ownGoals;
    @JsonProperty("penalties_saved")     private Integer penaltiesSaved;
    @JsonProperty("penalties_missed")    private Integer penaltiesMissed;
    @JsonProperty("yellow_cards")        private Integer yellowCards;
    @JsonProperty("red_cards")           private Integer redCards;
    private Integer saves;
    private Integer bonus;
    @JsonProperty("in_dreamteam")        private boolean inDreamteam;
    @JsonProperty("total_points")        private Integer totalPoints;
}
