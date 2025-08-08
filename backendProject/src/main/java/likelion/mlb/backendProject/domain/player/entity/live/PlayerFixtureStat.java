package likelion.mlb.backendProject.domain.player.entity.live;


import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveElementDto;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Entity
@Table(name = "player_fixture_stat")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Slf4j
public class PlayerFixtureStat {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private Integer minutes;
    private Integer goalsScored;
    private Integer assists;
    private Integer cleanSheets;
    private Integer goalsConceded;
    private Integer ownGoals;
    private Integer penaltiesSaved;
    private Integer penaltiesMissed;
    private Integer yellowCards;
    private Integer redCards;
    private Integer saves;
    private Integer bonus;
    private Boolean inDreamteam;
    private Integer totalPoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id")
    private Fixture fixture;


    public boolean updatePlayerFixtureStat(LiveElementDto element) {
        // 이전 값 저장 (이벤트 생성을 위해)
        int previousGoals = this.goalsScored;
        int previousAssists = this.assists;

        boolean hasChanges = false;

        // 모든 스탯 필드 업데이트 체크
        if (this.minutes != element.getStats().getMinutes()) {
            this.minutes = element.getStats().getMinutes();
            hasChanges = true;
        }
        if (this.goalsScored != element.getStats().getGoalsScored()) {
            this.goalsScored = element.getStats().getGoalsScored();
            hasChanges = true;
        }
        if (this.assists != element.getStats().getAssists()) {
            this.assists = element.getStats().getAssists();
            hasChanges = true;
        }
        if (this.cleanSheets != element.getStats().getCleanSheets()) {
            this.cleanSheets = element.getStats().getCleanSheets();
            hasChanges = true;
        }
        if (this.goalsConceded != element.getStats().getGoalsConceded()) {
            this.goalsConceded = element.getStats().getGoalsConceded();
            hasChanges = true;
        }
        if (this.ownGoals != element.getStats().getOwnGoals()) {
            this.ownGoals = element.getStats().getOwnGoals();
            hasChanges = true;
        }
        if (this.penaltiesSaved != element.getStats().getPenaltiesSaved()) {
            this.penaltiesSaved = element.getStats().getPenaltiesSaved();
            hasChanges = true;
        }
        if (this.penaltiesMissed != element.getStats().getPenaltiesMissed()) {
            this.penaltiesMissed = element.getStats().getPenaltiesMissed();
            hasChanges = true;
        }
        if (this.yellowCards != element.getStats().getYellowCards()) {
            this.yellowCards = element.getStats().getYellowCards();
            hasChanges = true;
        }
        if (this.redCards != element.getStats().getRedCards()) {
            this.redCards = element.getStats().getRedCards();
            hasChanges = true;
        }
        if (this.saves != element.getStats().getSaves()) {
            this.saves = element.getStats().getSaves();
            hasChanges = true;
        }
        if (this.bonus != element.getStats().getBonus()) {
            this.bonus = element.getStats().getBonus();
            hasChanges = true;
        }
        if (this.totalPoints != element.getStats().getTotalPoints()) {
            this.totalPoints = element.getStats().getTotalPoints();
            hasChanges = true;
        }

        return hasChanges;
    }

}
