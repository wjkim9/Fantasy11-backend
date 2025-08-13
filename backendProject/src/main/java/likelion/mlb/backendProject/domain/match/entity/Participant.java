package likelion.mlb.backendProject.domain.match.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.user.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "participant")
@Getter
@Setter
public class Participant {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_number", nullable = false)
    private short userNumber;

    @Column(name = "isDummy", nullable = false)
    private boolean dummy;

    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @Column(name = "rank")
    private Integer rank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "draft_id", nullable = false)
    private Draft draft;

    public void updateRank(int leaguePoint, int rank) {
        this.score = leaguePoint;
        this.rank = rank;

    }
}
