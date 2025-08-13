package likelion.mlb.backendProject.domain.player.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.domain.player.cache.dto.PlayerDto;
import likelion.mlb.backendProject.domain.team.entity.Team;
import likelion.mlb.backendProject.global.jpa.entity.BaseTime;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.FplElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "player")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Player extends BaseTime {

    @GeneratedValue
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    //fpl에 등록된 선수의 코드 -> opta_code는 p + code ex) p150242
    @Column(nullable = false)
    private Integer code;

    @Column(nullable = true)
    private Integer fplId;

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
    //x로 업데이트 된 건 fpl api로부터 받아온 데이터에서 삭제된 선수
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
                    .fplId(element.getFplId())
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

    public static Player playerBuilder(FplElement element,
                                             Map<Integer, ElementType> typeMap,
                                             Map<Integer, Team> teamMap) {

        String picUri = "https://resources.premierleague.com/premierleague25/photos/players/110x140/"
         + element.getCode() + ".png";
        return Player.builder()
                .code(element.getCode())
                .fplId(element.getFplId())
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
    }


    public void updatePlayer(FplElement element,
                             Map<Integer, ElementType> typeMap,
                             Map<Integer, Team> teamMap) {
        String picUri = "https://resources.premierleague.com/premierleague25/photos/players/110x140/"
                + element.getCode() + ".png";
        this.pic = picUri;
        this.status = element.getStatus();
        this.fplId = element.getFplId();
        this.news = element.getNews();
        this.teamCode = element.getTeamCode();
        this.chanceOfPlayingNextRound = element.isChanceOfPlayingNextRound();
        this.chanceOfPlayingThisRound = element.isChanceOfPlayingThisRound();
        this.etId = element.getElementType();
        this.team = teamMap.get(element.getTeam());
        this.elementType = typeMap.get(element.getElementType());
    }

    /**
     * Player 엔티티 리스트 → PlayerDto 리스트 변환
     */
    public static List<PlayerDto> toDtoList(List<Player> players) {
        return players.stream().map(p -> PlayerDto.builder()
                .id(p.getId())
                .webName(p.getWebName())
                .krName(p.getKrName())
                .pic(p.getPic())
                .status(p.getStatus())

                // team관련 설정
                .teamName(p.getTeam().getName())
                .teamKrName(p.getTeam().getKrName())

                // 포지션(elementType) 관련 설정
                .elementTypeId(p.getElementType().getId())
                .elementTypePluralName(p.getElementType().getPluralName())
                .elementTypeKrName(p.getElementType().getKrName())
                .build()
        ).collect(Collectors.toList());
    }

}

