package likelion.mlb.backendProject.domain.draft.entity;


import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.user.entity.User;
import likelion.mlb.backendProject.global.jpa.entity.BaseTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


/**
 * 참여자 엔티티
 * */
@Entity
@Table(name="participant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_number", nullable = false)
    private int userNumber; // 순번

    @Column(name = "isDummy", nullable = false)
    private boolean isDummy = false; // 더미 여부(bot 여부)

    @Column(name = "score", nullable = false)
    private int score; // 점수

    @Column(name = "rank", nullable = false)
    private int rank; // 순위

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 사용자 외래키
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "draft_id") // 드래프트방 외래키
    private Draft draft;

}
