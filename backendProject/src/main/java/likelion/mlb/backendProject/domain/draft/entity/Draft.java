package likelion.mlb.backendProject.domain.draft.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.global.staticdata.entity.Round;
import lombok.*;

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

    @Column(name = "room_no", nullable = false)
    private long roomNo;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;
}
