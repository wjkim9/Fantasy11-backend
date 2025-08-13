package likelion.mlb.backendProject.domain.round.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "round_score")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RoundScore {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    //해당 라운드에서 4명 드래프트방에서 얻은 승정 (0~3)
    private Integer score;

    //해당 라운드에 내 11명 선수가 받은 점수 총합
    private Integer points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id")
    private Round round;


    public static RoundScore RoundScoreBuilder(int score, int points, User user, Round round) {
        RoundScore roundScore = new RoundScore();
        roundScore.score = score;
        roundScore.points = points;
        roundScore.user = user;
        roundScore.round = round;
        return roundScore;
    }
}
