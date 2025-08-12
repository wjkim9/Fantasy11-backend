package likelion.mlb.backendProject.domain.draft.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.player.entity.Player;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "participant_player")
@Getter
@Setter
@Builder
public class ParticipantPlayer {

    @Id
    @Builder.Default
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id = UUID.randomUUID(); // save시 UUID값 자동 생성

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
}