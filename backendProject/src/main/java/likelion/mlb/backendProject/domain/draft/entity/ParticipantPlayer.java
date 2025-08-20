package likelion.mlb.backendProject.domain.draft.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.draft.dto.DraftResponse;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.player.entity.Player;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "participant_player")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    /**
     * Player 엔티티 리스트 → PlayerDto 리스트 변환
     */
    public static List<DraftResponse> toDtoList(List<ParticipantPlayer> players) {
        return players.stream().map(p -> DraftResponse.builder()
                .participantId(p.getParticipant().getId())
                .playerId(p.getPlayer().getId())
                .playerWebName(p.getPlayer().getWebName())
                .playerKrName(p.getPlayer().getKrName())
                .playerPic(p.getPlayer().getPic())

                // team관련 설정
                .teamId(p.getPlayer().getTeam().getId())
                .teamName(p.getPlayer().getTeam().getName())
                .teamKrName(p.getPlayer().getTeam().getKrName())

                // 포지션(elementType) 관련 설정
                .elementTypeId(p.getPlayer().getElementType().getId())
                .elementTypePluralName(p.getPlayer().getElementType().getPluralName())
                .elementTypeKrName(p.getPlayer().getElementType().getKrName())
                .build()
        ).collect(Collectors.toList());
    }
}