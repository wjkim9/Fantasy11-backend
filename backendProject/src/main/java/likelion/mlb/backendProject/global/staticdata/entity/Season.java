package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "season")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Season {

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

