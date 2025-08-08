package likelion.mlb.backendProject.domain.team.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.round.entity.Season;
import likelion.mlb.backendProject.global.jpa.entity.BaseTime;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.FplTeam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "team")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Team extends BaseTime {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false)
    private Integer code;

    @Column(name = "fpl_id", nullable = false)
    private Integer fplId;

    @Column(name = "name", nullable = false) //fpl에서 받아오는 영어 이름
    private String name;

    @Builder.Default
    @Column(name = "kr_name", nullable = false)
    private String krName = "";

    @Column(name = "win", nullable = false)
    private short win = 0;

    @Column(name = "draw", nullable = false)
    private short draw = 0;

    @Column(name = "lose", nullable = false)
    private short lose = 0;

    @Column(name = "points", nullable = false)
    private short points = 0;

    @Column(name = "played", nullable = false)
    private short played = 0;

    @Column(name = "position", nullable = false)
    private short position = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    public static List<Team> teamBuilder(List<FplTeam> fplTeams, Season season) {
        List<Team> ts = new ArrayList<>();
        for (FplTeam fplTeam : fplTeams) {
            Team t = Team.builder()
                    .code(fplTeam.getCode())
                    .fplId(fplTeam.getFplId())
                    .name(fplTeam.getName())
                    .played(fplTeam.getPlayed())
                    .win(fplTeam.getWin())
                    .draw(fplTeam.getDraw())
                    .lose(fplTeam.getLose())
                    .points(fplTeam.getPoints())
                    .position(fplTeam.getPosition())
                    .season(season)
                    .build();
            ts.add(t);
        }
        return ts;
    }

    public void updateStats(FplTeam team) {
        this.played = team.getPlayed();
        this.win = team.getWin();
        this.draw = team.getDraw();
        this.lose = team.getLose();
        this.points = team.getPoints();
        this.position = team.getPosition();
    }
}