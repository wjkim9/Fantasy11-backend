package likelion.mlb.backendProject.global.scheduler.service;

import likelion.mlb.backendProject.domain.chat.service.ChatNotificationService;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.entity.live.MatchEvent;
import likelion.mlb.backendProject.domain.player.entity.live.PlayerFixtureStat;
import likelion.mlb.backendProject.domain.player.repository.MatchEventRepository;
import likelion.mlb.backendProject.domain.player.repository.PlayerFixtureStatRepository;
import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;
import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.domain.round.entity.Round;
import likelion.mlb.backendProject.domain.round.repository.FixtureRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.global.configuration.FplClient;
import likelion.mlb.backendProject.global.staticdata.dto.fixture.FplFixture;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveElementDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveEventDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveFixtureDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.element.ExplainDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LiveDataService {

    private final FplClient fpl;
    private final RoundRepository roundRepository;
    private final FixtureRepository fixtureRepository;
    private final PlayerRepository playerRepository;
    private final PlayerFixtureStatRepository playerFixtureStatRepository;
    private final MatchEventRepository matchEventRepository;
    private final ChatNotificationService notificationService;

    public void pollLiveFixtures() {

        try {
            // 현재 진행중인 라운드 정보 가져옴
            Round currentRound = getCurrentRound();

            // 해당 라운드의 경기 정보를 API를 통해 받아옴
            LiveEventDto liveData = fetchLiveData(currentRound);
            List<FplFixture> fixtures = fpl.getFixtures(currentRound.getRound());
            // 진행중인 경기가 없으면 종료
            if (hasNoActiveFixtures(fixtures)) {
                log.info("진행 중인 경기가 없어 스케줄링을 종료합니다.");
                return;
            }

            // 경기 정보 업데이트
            int updatedFixtures = updateLiveFixtures(fixtures);
            log.info("업데이트된 경기 수: {}", updatedFixtures);

            // 선수 실시간 데이터 정보 업데이트
            int updatedPlayers = processPlayerEvents(liveData, fixtures);
            log.info("업데이트된 선수 수: {}", updatedPlayers);

        } catch (Exception e) {
            log.error("실시간 데이터 스케줄링 중 오류 발생", e);
        }

    }

    private Round getCurrentRound() {
        return roundRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new RuntimeException("현재 진행중인 라운드가 없습니다."));
    }

    private LiveEventDto fetchLiveData(Round currentRound) {
        return fpl.getLive(currentRound.getRound());
    }

    private boolean hasNoActiveFixtures(List<FplFixture> fixtures) {
        if (fixtures == null || fixtures.isEmpty()) return true;
        return fixtures.stream().noneMatch(f ->
                Boolean.TRUE.equals(f.isStarted()) || (f.getMinutes() != null && f.getMinutes() > 0));
    }

    private int updateLiveFixtures(List<FplFixture> fixtureList) {
        if (fixtureList == null || fixtureList.isEmpty()) {
            return 0;
        }

        // N+1 문제 해결을 위한 배치 조회
        List<Integer> fplIds = fixtureList.stream()
                .map(FplFixture::getFplId)
                .toList();

        Map<Integer, Fixture> fixtureMap = fixtureRepository.findAllByFplIdInAsMap(fplIds);
        int updatedCount = 0;

        for (FplFixture dto : fixtureList) {
            Fixture fixture = fixtureMap.get(dto.getFplId());
            if (fixture == null) {
                log.warn("매핑된 Fixture가 없습니다. fplId={}", dto.getFplId());
                continue;
            }

            // 변경사항이 있는 경우만 업데이트
            if (hasFixtureChanged(fixture, dto)) {
                fixture.updateLiveFixture(dto);
                updatedCount++;
                log.debug("Fixture 업데이트 완료 {}: started={}, finished={}, minutes={}, homeScore={}, awayScore={}",
                        fixture.getFplId(), fixture.isStarted(), fixture.isFinished(),
                        fixture.getMinutes(), dto.getHomeTeamScore(), dto.getAwayTeamScore());
            }
        }

        return updatedCount;
    }

//    private boolean hasFixtureChanged(Fixture fixture, FplFixture dto) {
//        return fixture.isStarted() != dto.isStarted() ||
//                fixture.isFinished() != dto.isFinished() ||
//                fixture.getMinutes() != dto.getMinutes() ||
//                !fixture.getHomeTeamScore().equals(dto.getHomeTeamScore()) ||
//                !fixture.getAwayTeamScore().equals(dto.getAwayTeamScore());
//    }
private boolean hasFixtureChanged(Fixture fixture, FplFixture dto) {
    // minutes가 Integer라면 언박싱 전에 기본값 처리
    int fMin = fixture.getMinutes() == null ? 0 : fixture.getMinutes();
    int dMin = dto.getMinutes() == null ? 0 : dto.getMinutes();

    // started/finished가 Boolean일 수도 있으니 null-safe 비교
    boolean startedChanged  = !Objects.equals(fixture.isStarted(),  dto.isStarted());
    boolean finishedChanged = !Objects.equals(fixture.isFinished(), dto.isFinished());

    boolean homeScoreChanged = !Objects.equals(fixture.getHomeTeamScore(), dto.getHomeTeamScore());
    boolean awayScoreChanged = !Objects.equals(fixture.getAwayTeamScore(), dto.getAwayTeamScore());
    boolean minutesChanged   = (fMin != dMin);

    return startedChanged || finishedChanged || homeScoreChanged || awayScoreChanged || minutesChanged;
}

    private int processPlayerEvents(LiveEventDto liveData, List<FplFixture> fixtures) {
        if (liveData.getElements() == null || liveData.getElements().isEmpty()) {
            log.info("처리할 선수 데이터가 없습니다.");
            return 0;
        }

        // 진행중인 경기 필터링
        List<Integer> activeFixtureIds = getActiveFixtureIds(fixtures);
        if (activeFixtureIds.isEmpty()) {
            log.info("진행중인 경기가 없습니다.");
            return 0;
        }

        // 필요한 데이터 배치 조회
        Map<Integer, Player> playerMap = getPlayerMap(liveData);
        Map<Integer, Fixture> fixtureMap = fixtureRepository.findAllByFplIdInAsMap(activeFixtureIds);
        Map<String, PlayerFixtureStat> existingStats = getExistingStats(activeFixtureIds);

        int updatedCount = 0;

        // 각 선수별로 스탯 처리
        for (LiveElementDto element : liveData.getElements()) {
            Player player = playerMap.get(element.getPlayerId());
            if (player == null) {
                log.warn("매핑된 Player가 없습니다. fplId={}", element.getPlayerId());
                continue;
            }

            // 각 경기별로 선수 스탯 처리
            for (ExplainDto explainItem : element.getExplain()) {
                if (!activeFixtureIds.contains(explainItem.getFixture())) {
                    continue; // 진행중인 경기만 처리
                }

                String statKey = player.getFplId() + "_" + explainItem.getFixture();
                PlayerFixtureStat existingStat = existingStats.get(statKey);

                if (existingStat != null) {
                    // 기존 스탯 업데이트
                    if (updatePlayerFixtureStat(existingStat, element)) {
                        updatedCount++;
                    }
                } else {
                    // 새로운 스탯 생성
                    Fixture fixture = fixtureMap.get(explainItem.getFixture());
                    if (fixture == null) {
                        log.warn("매핑된 Fixture가 없습니다. fplId={}", explainItem.getFixture());
                        continue;
                    }

                    PlayerFixtureStat newStat = createPlayerFixtureStat(player, fixture, element);
                    playerFixtureStatRepository.save(newStat);
                    updatedCount++;

                    // 초기 이벤트 생성 (기존에 골/어시스트가 있는 경우)
                    createInitialMatchEvents(newStat, element);
                }
            }
        }

        return updatedCount;
    }

    private List<Integer> getActiveFixtureIds(List<FplFixture> liveData) {
        return liveData.stream()
                .filter(fixture -> fixture.isStarted() && !fixture.isFinished())
                .map(FplFixture::getFplId)
                .toList();
    }

    private Map<Integer, Player> getPlayerMap(LiveEventDto liveData) {
        List<Integer> playerFplIds = liveData.getElements().stream()
                .map(LiveElementDto::getPlayerId)
                .toList();
        return playerRepository.findAllByFplIdInAsMap(playerFplIds);
    }

    private Map<String, PlayerFixtureStat> getExistingStats(List<Integer> activeFixtureIds) {
        return playerFixtureStatRepository.findByFixtureFplIds(activeFixtureIds)
                .stream()
                .collect(Collectors.toMap(
                        stat -> stat.getPlayer().getFplId() + "_" + stat.getFixture().getFplId(),
                        stat -> stat
                ));
    }

    private boolean updatePlayerFixtureStat(PlayerFixtureStat stat, LiveElementDto element) {
        // 이전 값 저장 (이벤트 생성을 위해)
        int previousGoals = stat.getGoalsScored();
        int previousAssists = stat.getAssists();

        boolean hasChanges = stat.updatePlayerFixtureStat(element);

        if (hasChanges) {
            log.debug("PlayerFixtureStat 업데이트: playerId={}, fixtureId={}, points={}",
                    stat.getPlayer().getFplId(), stat.getFixture().getFplId(), stat.getTotalPoints());

            // 골/어시스트 이벤트 생성 체크
            createMatchEventsIfNeeded(stat, previousGoals, previousAssists,
                    element.getStats().getGoalsScored(), element.getStats().getAssists());
        }

        return hasChanges;
    }

    private PlayerFixtureStat createPlayerFixtureStat(Player player, Fixture fixture,
                                                      LiveElementDto element) {
        return PlayerFixtureStat.builder()
                .player(player)
                .fixture(fixture)
                .minutes(element.getStats().getMinutes())
                .goalsScored(element.getStats().getGoalsScored())
                .assists(element.getStats().getAssists())
                .cleanSheets(element.getStats().getCleanSheets())
                .goalsConceded(element.getStats().getGoalsConceded())
                .ownGoals(element.getStats().getOwnGoals())
                .penaltiesSaved(element.getStats().getPenaltiesSaved())
                .penaltiesMissed(element.getStats().getPenaltiesMissed())
                .yellowCards(element.getStats().getYellowCards())
                .redCards(element.getStats().getRedCards())
                .saves(element.getStats().getSaves())
                .bonus(element.getStats().getBonus())
                .totalPoints(element.getStats().getTotalPoints())
                .build();
    }

    private void createMatchEventsIfNeeded(PlayerFixtureStat stat, int previousGoals, int previousAssists,
                                           int currentGoals, int currentAssists) {
        int currentMatchMinute = stat.getFixture().getMinutes();

        // 새로운 골 이벤트 생성
        if (currentGoals > previousGoals) {
            int newGoals = currentGoals - previousGoals;
            for (int i = 0; i < newGoals; i++) {
                createMatchEvent(stat, "goals_scored", currentMatchMinute, (short) 4);
                log.info("골 이벤트 생성: player={}, fixture={}, matchMinute={}, point={}",
                        stat.getPlayer().getFplId(), stat.getFixture().getFplId(), currentMatchMinute, 4);
            }
        }

        // 새로운 어시스트 이벤트 생성
        if (currentAssists > previousAssists) {
            int newAssists = currentAssists - previousAssists;
            for (int i = 0; i < newAssists; i++) {
                createMatchEvent(stat, "assist", currentMatchMinute, (short) 3);
                log.info("어시스트 이벤트 생성: player={}, fixture={}, matchMinute={}, point={}",
                        stat.getPlayer().getFplId(), stat.getFixture().getFplId(), currentMatchMinute, 3);
            }
        }
    }

    private void createInitialMatchEvents(PlayerFixtureStat stat, LiveElementDto element) {
        int currentMatchMinute = stat.getFixture().getMinutes();

        // 처음 생성시 기존 골/어시스트가 있다면 이벤트로 생성
        for (int i = 0; i < element.getStats().getGoalsScored(); i++) {
            createMatchEvent(stat, "goals_scored", currentMatchMinute, (short) 4);

            log.info("초기 골 이벤트 생성: player={}, fixture={}, matchMinute={}, point={}",
                    stat.getPlayer().getFplId(), stat.getFixture().getFplId(), currentMatchMinute, 4);

        }

        for (int i = 0; i < element.getStats().getAssists(); i++) {
            createMatchEvent(stat, "assist", currentMatchMinute, (short) 3);
            log.info("초기 어시스트 이벤트 생성: player={}, fixture={}, matchMinute={}, point={}",
                    stat.getPlayer().getFplId(), stat.getFixture().getFplId(), currentMatchMinute, 3);
        }
    }

    private void createMatchEvent(PlayerFixtureStat stat, String eventType, int minute, short point) {
        MatchEvent matchEvent = MatchEvent.builder()
                .fixture(stat.getFixture())
                .player(stat.getPlayer())
                .eventType(eventType)
                .minute(minute)
                .point(point)
                .build();

        MatchEvent saved = matchEventRepository.save(matchEvent);

        //notificationService.sendMatchAlert(saved);

    }
}
