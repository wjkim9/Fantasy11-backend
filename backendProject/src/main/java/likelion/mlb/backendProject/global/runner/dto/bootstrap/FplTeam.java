package likelion.mlb.backendProject.global.runner.dto.bootstrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FplTeam {

    //fpl에서 제공하는 팀 코드
    private Integer code;

    private String name;

    @JsonProperty("id")
    private Integer fplId;
    private short played;
    private short win;
    private short draw;
    private short lose;
    private short points;
    private short position;

}
