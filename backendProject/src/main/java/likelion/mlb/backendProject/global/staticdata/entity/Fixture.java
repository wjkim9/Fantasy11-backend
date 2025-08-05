package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.global.runner.dto.fixture.FplFixture;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "fixture")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Fixture {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "code", nullable = false)
    private long code;

    @Column(name = "fpl_id", nullable = false)
    private Integer fplId;

    @Column(name = "kickoff_time", nullable = false)
    private OffsetDateTime kickoffTime;

    @Column(name = "home_team_score")
    private Integer homeTeamScore;

    @Column(name = "away_team_score")
    private Integer awayTeamScore;


    @Column(name = "started", nullable = false)
    private boolean started = false;

    @Column(name = "minutes", nullable = false)
    private short minutes = 0;

    @Column(name = "finished", nullable = false)
    private boolean finished = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    public static List<Fixture> fixtureBuilder(List<FplFixture> fplFixtures,
                                               Map<Integer, Team> teamMap,
                                               Map<Short, Round> roundMap) {
        List<Fixture> fixtures = new ArrayList<>();

        for (FplFixture fplFixture : fplFixtures) {
            Fixture fixture = Fixture.builder()
                    .code(fplFixture.getCode())
                    .fplId(fplFixture.getFplId())
                    .started(fplFixture.isStarted())
                    .finished(fplFixture.isFinished())
                    .homeTeamScore(fplFixture.getHomeTeamScore())
                    .awayTeamScore(fplFixture.getAwayTeamScore())
                    .kickoffTime(fplFixture.getKickoffTime())
                    .minutes(fplFixture.getMinutes())
                    .round(roundMap.get(fplFixture.getEvent()))
                    .homeTeam(teamMap.get(fplFixture.getHomeTeam()))
                    .awayTeam(teamMap.get(fplFixture.getAwayTeam()))
                    .build();
            fixtures.add(fixture);
        }
        return fixtures;
    }
}
