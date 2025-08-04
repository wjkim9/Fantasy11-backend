package likelion.mlb.backendProject.domain.draft.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.global.staticdata.entity.Round;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "draft")
@Getter
@Setter
public class Draft {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "room_num", nullable = false)
    private int roomNum = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;
}