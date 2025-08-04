package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "player")
@Getter
@Setter
public class Player {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "web_name", nullable = false)
    private String webName;

    @Column(name = "kr_name", nullable = false)
    private String krName;

    @Column(name = "now_cost", nullable = false)
    private short nowCost = 0;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "news")
    private String news;

    @Column(name = "pic")
    private String pic;

    @Column(name = "opta_code", nullable = false)
    private String optaCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "element_type_id", nullable = false)
    private ElementType elementType;
}

