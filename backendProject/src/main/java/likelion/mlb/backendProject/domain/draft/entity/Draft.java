package likelion.mlb.backendProject.domain.draft.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.round.entity.Round;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "draft")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Draft {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "room_no", nullable = false, updatable = false, insertable = false)
    @org.hibernate.annotations.Generated(org.hibernate.annotations.GenerationTime.INSERT)
    private Long roomNo;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @OneToMany(mappedBy = "draft")
    private List<Participant> participants = new ArrayList<>();
}
