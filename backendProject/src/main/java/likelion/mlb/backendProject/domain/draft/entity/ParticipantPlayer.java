package likelion.mlb.backendProject.domain.draft.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.global.staticdata.entity.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "participant_player")
@Getter
@Setter
public class ParticipantPlayer {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
}