package likelion.mlb.backendProject.domain.user.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.round.entity.Season;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "season_user_score")
@Getter
@Setter
public class SeasonUserScore {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "score", nullable = false)
    private short score = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;
}