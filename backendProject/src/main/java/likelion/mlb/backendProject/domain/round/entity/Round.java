package likelion.mlb.backendProject.domain.round.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.global.jpa.entity.BaseTime;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.FplEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "round")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Round extends BaseTime {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name ="round", nullable = false)
    private Integer round;

    // ex) Gameweek 1
    @Column(nullable = false)
    private String name;

    //해당 라운드의 모든 경기가 종료됐는지
    @Column(nullable = false)
    private boolean finished;

    //fpl 점수 기준 해당 라운드에서 가장 점수를 많이 낸 선수의 id
    @Column(name ="top_element")
    private Integer topElement;

    //fpl 기준 해당 라운드에서 가장 많이 뽑힌 선수의 id
    @Column(name = "most_transferred_id")
    private Integer mostTransferredIn;

    //“직전 라운드(바로 이전 라운드)” 라면 true
    @Column(nullable = false)
    private boolean isPrevious;
    //“현재 진행 중인 라운드” 라면 true
    @Column(nullable = false)
    private boolean isCurrent;
    //“다음으로 진행될 라운드” 라면 true
    @Column(nullable = false)
    private boolean isNext;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "ended_at")
    private OffsetDateTime endedAt;

    @Column(name = "settled")
    private Boolean settled;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    public static List<Round> roundBuilder(List<FplEvent> events, Season season) {
        List<Round> rounds = new ArrayList<>();
        for (FplEvent event : events) {
            Round r = Round.builder()
                    .round(event.getFplId())
                    .name(event.getName())
                    .finished(event.isFinished())
                    .topElement(event.getTopElement())
                    .mostTransferredIn(event.getMostTransferredIn())
                    .isPrevious(event.isPrevious())
                    .isCurrent(event.isCurrent())
                    .isNext(event.isNext())
                    .season(season)
                    .build();
            rounds.add(r);
        }
        return rounds;
    }

    public void setRoundTime(OffsetDateTime earliest, OffsetDateTime latest) {
        this.startedAt = earliest;
        this.endedAt = latest;
    }

    public void updateRound(FplEvent event) {
        this.finished = event.isFinished();
        this.topElement = event.getTopElement();
        this.mostTransferredIn = event.getMostTransferredIn();
        this.isPrevious = event.isPrevious();
        this.isCurrent = event.isCurrent();
        this.isNext = event.isNext();
    }

    public void changeSettled() {
        this.settled = true;
    }
}