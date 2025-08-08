package likelion.mlb.backendProject.domain.player.entity.live;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "explain")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Explain {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String stat;

    private Integer value;

    private Integer points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pfs_id")
    private PlayerFixtureStat playerFixtureStat;
}
