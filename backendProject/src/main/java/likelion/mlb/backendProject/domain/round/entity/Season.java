package likelion.mlb.backendProject.domain.round.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.global.jpa.entity.BaseTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "season")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Season extends BaseTime {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "season_name", nullable = false)
    private String seasonName;

    public void setSeason(String seasonName) {
        this.seasonName = seasonName;
    }
}

