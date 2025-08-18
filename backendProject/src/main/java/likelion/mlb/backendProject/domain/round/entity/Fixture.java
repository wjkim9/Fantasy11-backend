package likelion.mlb.backendProject.domain.round.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.team.entity.Team;
import likelion.mlb.backendProject.global.jpa.entity.BaseTime;
import likelion.mlb.backendProject.global.staticdata.dto.fixture.FplFixture;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveFixtureDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
public class Fixture extends BaseTime {

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
    private Integer minutes = 0;

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
                                               Map<Integer, Round> roundMap) {
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


    /**
     * 하루에 한 번 경기 업데이트
     * @param fplFixture
     * @param teamMap
     * @param roundMap
     */
    public void updateFixture(FplFixture fplFixture,
                              Map<Integer, Team> teamMap,
                              Map<Integer, Round> roundMap) {
        this.started = fplFixture.isStarted();
        this.finished = fplFixture.isFinished();
        this.homeTeamScore = fplFixture.getHomeTeamScore();
        this.awayTeamScore = fplFixture.getAwayTeamScore();
        this.kickoffTime = fplFixture.getKickoffTime();
        this.minutes = fplFixture.getMinutes();
        this.round = roundMap.get(fplFixture.getEvent());
        this.homeTeam = teamMap.get(fplFixture.getHomeTeam());
        this.awayTeam = teamMap.get(fplFixture.getAwayTeam());
    }

    /**
     * 실시간으로 진행 중인 경기 정보 업데이트
     */
    public void updateLiveFixture(FplFixture dto) {
        this.started = dto.isStarted();
        this.finished = dto.isFinished();
        this.homeTeamScore = dto.getHomeTeamScore();
        this.awayTeamScore = dto.getAwayTeamScore();
        this.minutes = dto.getMinutes();
    }
}
