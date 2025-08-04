package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "round")
@Getter
@Setter
public class Round {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "round", nullable = false)
    private short round;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;
}