package likelion.mlb.backendProject.domain.draft.entity;


import jakarta.persistence.*;
import likelion.mlb.backendProject.global.jpa.entity.BaseTime;
import lombok.*;

import java.util.UUID;


/**
 * 드래프트방 엔티티
 * */
@Entity
@Table(name="draft")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Draft {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id") // 라운드 외래키
    private Round round;
    */

}
