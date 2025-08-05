package likelion.mlb.backendProject.global.staticdata.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.global.runner.dto.bootstrap.FplElement;
import lombok.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "player")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Player {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    //fpl에 등록된 선수의 코드 -> opta_code는 p + code ex) p150242
    @Column(nullable = false)
    private Integer code;

    @Column(name = "web_name", nullable = false) //fpl에서 받아오는 영어 이름
    private String webName;

    @Builder.Default
    @Column(name = "kr_name", nullable = false)
    private String krName = "";

    @Column(name = "pic", nullable = false, columnDefinition = "TEXT")
    private String pic;  // == DB에 저장될 때 TEXT

    //다음 라운드에 뛸 수 있는지
    @Column(nullable = false)
    private boolean chanceOfPlayingNextRound;

    //해당 라운드에 뛸 수 있는지
    @Column(nullable = false)
    private boolean chanceOfPlayingThisRound;

    @Column(name = "cost", nullable = false)
    private short cost = 0;

    //a: 뛸 수 있음, d: 출전 불투명, i: 부상, s: 징계, u: 사용불(임대 등), n: 자격 없음(미등록 선수)
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "news", nullable = false)
    private String news = "";

    @Column(name = "team_code", nullable = false)
    private Integer teamCode;

    //fpl로부터 받아오는 elementType 아이디
    @Column(name = "et_id", nullable = false)
    private Integer etId;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "element_type_id", nullable = false)
    private ElementType elementType;


    public static List<Player> playerBuilder(List<FplElement> elements,
                                             Map<Integer, ElementType> typeMap,
                                             Map<Integer, Team> teamMap) {
        List<Player> players = new ArrayList<>();
        for (FplElement element : elements) {
            String picUri = "https://resources.premierleague.com/premierleague/photos/players/250x250/p"
                    + element.getCode() + ".png";
            Player player = Player.builder()
                    .code(element.getCode())
                    .webName(element.getWebName())
                    .pic(picUri)
                    .chanceOfPlayingNextRound(element.isChanceOfPlayingNextRound())
                    .chanceOfPlayingThisRound(element.isChanceOfPlayingThisRound())
                    .news(element.getNews())
                    .status(element.getStatus())
                    .teamCode(element.getTeamCode())
                    .cost(element.getCost())
                    .etId(element.getElementType())
                    .elementType(typeMap.get(element.getElementType()))
                    .team(teamMap.get(element.getTeam()))
                    .build();
            players.add(player);
        }
        return players;
    }
}

