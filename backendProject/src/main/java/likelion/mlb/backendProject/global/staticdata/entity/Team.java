package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "team")
@Getter
@Setter
public class Team {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "kr_name", nullable = false)
    private String krName;

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
}