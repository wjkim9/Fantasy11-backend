package likelion.mlb.backendProject.domain.match.entity;

import jakarta.persistence.*;

import likelion.mlb.backendProject.domain.draft.dto.DraftParticipant;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.user.entity.User;

import lombok.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "participant")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "user_number", nullable = false)
  private short userNumber;

  @Column(name = "is_dummy", nullable = false)
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

  /**
   * Participant 엔티티 리스트 → DraftParticipant 리스트 변환
   */
  public static List<DraftParticipant> toDtoList(List<Participant> participants) {

    return participants.stream().map(p -> DraftParticipant.builder()

        .participantId(p.getId())
        .participantUserNumber(p.getUserNumber())
        .participantDummy(p.isDummy())

        .userEmail(p.getUser() != null ? p.getUser().getEmail() : null)
        .userName(p.getUser() != null ? p.getUser().getName() : null)

        .build()
    ).collect(Collectors.toList());
  }
}
