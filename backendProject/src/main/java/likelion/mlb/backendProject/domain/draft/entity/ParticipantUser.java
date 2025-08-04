package likelion.mlb.backendProject.domain.draft.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


/**
 * 참여자 선수 엔티티
 * : 드래프트방에 들어온 참여자가 뽑은 선수 정보를 담는 엔티티
 * */
@Entity
@Table(name="participant_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantUser {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id") // 참여자 외래키
    private Participant participant;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id") // 선수 외래키
    private Player player;
    */
}
