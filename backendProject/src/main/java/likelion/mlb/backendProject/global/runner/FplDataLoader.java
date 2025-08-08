package likelion.mlb.backendProject.global.runner;

import likelion.mlb.backendProject.global.runner.dto.bootstrap.BootstrapStatic;
import likelion.mlb.backendProject.global.runner.dto.fixture.FplFixture;
import likelion.mlb.backendProject.global.staticdata.entity.*;
import likelion.mlb.backendProject.global.staticdata.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//@Component
@RequiredArgsConstructor
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
    public void run(ApplicationArguments args) throws Exception {
        var bootstrap = fpl.get()
                .uri("/bootstrap-static/")
                .retrieve()
                .bodyToMono(BootstrapStatic.class)
                .block();

//        Season season = new Season();
//        season.setSeason("2526");
//        seasonRepository.save(season);

        //라운드 -> 포지션 -> 팀 -> 선수 -> 경기 순으로 db에 저장
        saveData(bootstrap);

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
