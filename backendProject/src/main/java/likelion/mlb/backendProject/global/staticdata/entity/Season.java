package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "season")
@Getter
@Setter
public class Season {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "season_name", nullable = false)
    private String seasonName;
}

