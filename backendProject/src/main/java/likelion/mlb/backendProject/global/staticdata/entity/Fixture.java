package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fixture")
@Getter
@Setter
public class Fixture {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false)
    private long code;

    @Column(name = "kickoff_time", nullable = false)
    private LocalDateTime kickoffTime;

    @Column(name = "team_h_score", nullable = false)
    private short teamHScore = 0;

    @Column(name = "team_a_score", nullable = false)
    private short teamAScore = 0;

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
}
