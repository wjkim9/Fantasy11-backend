package likelion.mlb.backendProject.global.staticdata;

import likelion.mlb.backendProject.domain.player.entity.ElementType;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.repository.ElementTypeRepository;
import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;
import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.domain.round.entity.Round;
import likelion.mlb.backendProject.domain.round.entity.Season;
import likelion.mlb.backendProject.domain.round.repository.FixtureRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.round.repository.SeasonRepository;
import likelion.mlb.backendProject.domain.team.entity.Team;
import likelion.mlb.backendProject.domain.team.repository.TeamRepository;
import likelion.mlb.backendProject.global.aop.SchedulerLog;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.BootstrapStatic;
import likelion.mlb.backendProject.global.staticdata.dto.fixture.FplFixture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 최초의 데이터를 db에 저장할 때 사용하는 컴포넌트
 */
//@Component
@RequiredArgsConstructor
@Slf4j
public class FplDataLoader implements ApplicationRunner {
    private final WebClient fpl;
    private final TeamRepository teamRepository;
    private final ElementTypeRepository typeRepository;
    private final PlayerRepository playerRepository;
    private final RoundRepository roundRepository;
    private final FixtureRepository fixtureRepository;
    private final SeasonRepository seasonRepository;
    @Override
    @Transactional
    @SchedulerLog(action = "initialDataScheduling")
    public void run(ApplicationArguments args) throws Exception {

        log.info("========================초기 데이터 저장 시작=================================");

        var bootstrap = fpl.get()
                .uri("/bootstrap-static/")
                .retrieve()
                .bodyToMono(BootstrapStatic.class)
                .block();

        Season season = new Season();
        season.setSeason("2526");
        seasonRepository.save(season);

        //라운드 -> 포지션 -> 팀 -> 선수 -> 경기 순으로 db에 저장
        saveData(bootstrap);


        log.info("========================초기 데이터 저장 종료=================================");


    }

    private void saveData(BootstrapStatic bootstrap) {
        Season season = seasonRepository.findBySeasonName("2526");
        //라운드 -> 포지션 -> 팀 -> 선수 -> 경기 순으로 db에 저장
        List<Round> savedRounds = roundRepository.saveAll(Round.roundBuilder(bootstrap.getEvents(), season));
        List<ElementType> savedTypes = typeRepository.saveAll(ElementType.elementTypeBuilder(bootstrap.getElementTypes()));
        List<Team> savedTeams = teamRepository.saveAll(Team.teamBuilder(bootstrap.getFplTeams(), season));


        //fixture에 매핑을 하기 위해 뽑아야 함
        Map<Integer, Round> roundMap = savedRounds.stream()
                .collect(Collectors.toMap(Round::getRound, Function.identity()));
        //player에 매핑을 하기 위해 뽑아야 함
        Map<Integer, ElementType> typeMap = savedTypes.stream()
                .collect(Collectors.toMap(ElementType::getFplId, Function.identity()));
        Map<Integer, Team> teamMap = savedTeams.stream()
                .collect(Collectors.toMap(Team::getFplId, Function.identity()));


        playerRepository.saveAll(Player.playerBuilder(bootstrap.getElements(), typeMap, teamMap));

        //1라운드씩 (10경기씩) 저장
        for (int i = 1; i <= savedRounds.size(); i++) {
            List<FplFixture> fplFixtures = fpl.get()
                    .uri("/fixtures/?event=" + i)
                    .retrieve()
                    .bodyToFlux(FplFixture.class)  // 배열 안의 요소 타입
                    .collectList()
                    .block();

            fixtureRepository.saveAll(Fixture.fixtureBuilder(fplFixtures, teamMap, roundMap));

            //각 라운드의 첫 경기 시작 시간, 마지막 경기 시작 시간 데이터 저장
            setRoundTime(savedRounds);
        }
    }

    private void setRoundTime(List<Round> rounds) {
        for (Round round : rounds) {
            // 2) 이 라운드의 모든 경기 가져오기
            List<Fixture> fixtures = fixtureRepository.findByRound(round);

            if (fixtures.isEmpty()) continue;

            // 3) 킥오프 타임만 뽑아서 min/max 계산
            OffsetDateTime earliest = fixtures.stream()
                    .map(Fixture::getKickoffTime)
                    .min(OffsetDateTime::compareTo)
                    .orElseThrow();

            OffsetDateTime latest = fixtures.stream()
                    .map(Fixture::getKickoffTime)
                    .max(OffsetDateTime::compareTo)
                    .orElseThrow();

            // 4) 필드 세팅
            round.setRoundTime(earliest, latest);
        }
    }
}
