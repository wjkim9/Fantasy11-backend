package likelion.mlb.backendProject.global.scheduler.service;

import likelion.mlb.backendProject.domain.player.entity.ElementType;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.entity.live.PlayerFixtureStat;
import likelion.mlb.backendProject.domain.player.repository.ElementTypeRepository;
import likelion.mlb.backendProject.domain.player.repository.PlayerFixtureStatRepository;
import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;
import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.domain.round.entity.Round;
import likelion.mlb.backendProject.domain.round.repository.FixtureRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.team.entity.Team;
import likelion.mlb.backendProject.domain.team.repository.TeamRepository;
import likelion.mlb.backendProject.global.configuration.FplClient;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.BootstrapStatic;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.FplElement;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.FplEvent;
import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.FplTeam;
import likelion.mlb.backendProject.global.staticdata.dto.fixture.FplFixture;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveElementDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveEventDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.element.ExplainDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DataUpdaterService {

    private final FplClient fpl;
    private final FixtureRepository fixtureRepository;
    private final RoundRepository roundRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final ElementTypeRepository elementTypeRepository;
    private final PlayerFixtureStatRepository playerFixtureStatRepository;


    public void fullRefresh() {


        // 1) bootstrap-static 재조회: teams / elements(선수) / events / element_types
        var bs = fpl.getBootstrapStatic();

        Map<Integer, ElementType> typeMap = elementTypeRepository.findAll().stream()
                .collect(Collectors.toMap(ElementType::getFplId, Function.identity()));
        Map<Integer, Team> teamMap = teamRepository.findAll().stream()
                .collect(Collectors.toMap(Team::getFplId, Function.identity()));

        // 2) 팀 업데이트 (승·무·패·승점·순위 등)
        updateTeam(bs.getFplTeams());

        // 3) 선수 이적 처리 (upsert + delete)
        updatePlayer(bs.getElements(), typeMap, teamMap);

        // 4) 경기시간 변동 반영
        updateRoundAndFixture(bs.getEvents(), teamMap);

    }

    /**
     * 승, 무, 패 등 팀 테이블의 컬럼 업데이트
     * @param
     */
    private void updateTeam(List<FplTeam> dtos) {
        List<Integer> fplIds = dtos.stream()
                .map(FplTeam::getFplId)
                .toList();
        List<Team> teams = teamRepository.findAllByFplIdIn(fplIds);

        // 2) Map< fplId, Team > 으로 재구성
        Map<Integer, Team> teamMap = teams.stream()
                .collect(Collectors.toMap(Team::getFplId, Function.identity()));

        // 3) dto 순회하면서 in-memory 업데이트
        for (FplTeam dto : dtos) {
            Team team = teamMap.get(dto.getFplId());
            if (team != null) {
                team.updateStats(dto);
            } else {
                log.warn("DB에 없는 팀 FPL ID: {}", dto.getFplId());
            }
        }
    }

    /**
     * 삭제된 선수는 status = "x" 로 변경
     * 기존 선수는 업데이트
     * 새롭게 생긴 선수는 insert
     * @param
     */
    private void updatePlayer(List<FplElement> elements,
                              Map<Integer, ElementType> typeMap,
                              Map<Integer, Team> teamMap) {

        // 1) DTO 에서 모든 코드 수집
        List<Integer> allFplIds = elements.stream()
                .map(FplElement::getFplId)
                .toList();

        // 2) 한 번에 DB 조회: 기존 선수들만
        List<Player> existingPlayers = playerRepository.findAllByFplIdIn(allFplIds);
        Map<Integer, Player> existingMap = existingPlayers.stream()
                .collect(Collectors.toMap(Player::getFplId, Function.identity()));

        // 3) 삭제 처리 (DB에 남아있으나 allCodes 에 없는 선수들)
        playerRepository.markDeletedByFplIdNotIn(allFplIds);

        // 4) 새로 추가할 선수들 모아두기
        List<Player> toInsert = new ArrayList<>();

        // 5) DTO 순회하면서 in-memory upsert
        for (FplElement dto : elements) {
            Player existing = existingMap.get(dto.getFplId());
            if (existing != null) {
                // 기존 선수면 필드만 업데이트 (dirty-checking 으로 커밋 시점에 UPDATE)
                existing.updatePlayer(dto, typeMap, teamMap);
            } else {
                // 새 선수면 리스트에 담아두기
                Player p = Player.playerBuilder(dto, typeMap, teamMap);
                toInsert.add(p);
            }
        }

        // 6) 새 선수 한 번에 저장
        if (!toInsert.isEmpty()) {
            playerRepository.saveAll(toInsert);
        }
    }

    /**
     * 각 경기의 시간을 업데이트
     * 업데이트가 됐다면 Round의 start_at, ended_at도 변경 + 각 라운드 및 경기가 끝났는지 finished 로 확인
     * 순서: round 업데이트 -> fixture 업데이트 -> fixture 기반으로 round 시작 시간, 종료 시간 업데이트
     * @param events
     */
    private void updateRoundAndFixture(List<FplEvent> events,
                                       Map<Integer, Team> teamMap) {
        // 1) 한 번에 DB에서 Round 엔티티 전부 조회
        List<Integer> eventIds = events.stream()
                .map(FplEvent::getFplId)
                .toList();
        Map<Integer, Round> roundMap = roundRepository
                .findAllByRoundIn(eventIds)
                .stream()
                .collect(Collectors.toMap(Round::getRound, Function.identity()));

        // 2) DTO 순회하며 in-memory 업데이트
        for (FplEvent evt : events) {
            Round round = roundMap.get(evt.getFplId());
            if (round != null) {
                round.updateRound(evt);
            } else {
                log.warn("DB에 없는 라운드: {}", evt.getFplId());
            }
        }

        // 3) Fixture 업데이트 — 모든 라운드에 대해 API 호출을 한 번에 모아서
        //    (실제 API는 라운드별로 호출해야 하겠지만, DB 조회만 batching)
        List<FplFixture> allFplFixtures = new ArrayList<>();
        for (Integer rid : roundMap.keySet()) {
            List<FplFixture> fl = fpl.getFixtures(rid);
            if (fl != null) allFplFixtures.addAll(fl);
        }

        // 4) DB에서 해당 Fixture 전부 한 번에 가져오기
        List<Integer> fixtureIds = allFplFixtures.stream()
                .map(FplFixture::getFplId)
                .toList();
        Map<Integer, Fixture> fixtureMap = fixtureRepository
                .findAllByFplIdIn(fixtureIds) // List<Fixture> findAllByFplIdIn(List<Integer> ids);
                .stream()
                .collect(Collectors.toMap(Fixture::getFplId, Function.identity()));

        // 5) in-memory 업데이트
        for (FplFixture ff : allFplFixtures) {
            Fixture fixture = fixtureMap.get(ff.getFplId());
            Round   round   = roundMap.get(ff.getEvent());
            if (fixture != null && round != null) {
                fixture.updateFixture(ff, teamMap, roundMap);
            } else {
                log.warn("매핑 누락: fixture {} 또는 round {}", ff.getFplId(), ff.getEvent());
            }
        }

        // 6) Round 시간 갱신
        setRoundTime(new ArrayList<>(roundMap.values()));
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


    /**
     * 여기서부턴 각 경기 선수의 보너스 점수를 위한 메소드
     * @param liveData
     * @param fixtures
     * @return
     */
    private int processPlayerEvents(LiveEventDto liveData, List<FplFixture> fixtures) {
        if (liveData.getElements() == null || liveData.getElements().isEmpty()) {
            log.info("처리할 선수 데이터가 없습니다.");
            return 0;
        }

        // 진행중인 경기 필터링
        List<Integer> finishFixtureIds = getFinishFixtureIds(fixtures);
        if (finishFixtureIds.isEmpty()) {
            log.info("진행중인 경기가 없습니다.");
            return 0;
        }

        // 필요한 데이터 배치 조회
        Map<Integer, Player> playerMap = getPlayerMap(liveData);
        Map<String, PlayerFixtureStat> existingStats = getExistingStats(finishFixtureIds);

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
                if (!finishFixtureIds.contains(explainItem.getFixture())) {
                    continue; // 진행중인 경기만 처리
                }

                String statKey = player.getFplId() + "_" + explainItem.getFixture();
                PlayerFixtureStat existingStat = existingStats.get(statKey);

                if (existingStat != null) {
                    // 기존 스탯 업데이트
                    if (updatePlayerFixtureStat(existingStat, element)) {
                        updatedCount++;
                    }
                }
            }
        }

        return updatedCount;
    }

    private List<Integer> getFinishFixtureIds(List<FplFixture> liveData) {
        return liveData.stream()
                .filter(fixture -> fixture.isStarted() && fixture.isFinished())
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

        boolean hasChanges = stat.updatePlayerFixtureStat(element);

        if (hasChanges) {
            log.debug("PlayerFixtureStat 업데이트: playerId={}, fixtureId={}, points={}",
                    stat.getPlayer().getFplId(), stat.getFixture().getFplId(), stat.getTotalPoints());
        }

        return hasChanges;
    }

}
