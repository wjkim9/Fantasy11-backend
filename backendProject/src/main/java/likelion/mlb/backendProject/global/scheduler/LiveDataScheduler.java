package likelion.mlb.backendProject.global.scheduler;

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
import likelion.mlb.backendProject.global.aop.SchedulerLog;
import likelion.mlb.backendProject.global.scheduler.service.LiveDataService;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveElementDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveEventDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveFixtureDto;
import likelion.mlb.backendProject.global.staticdata.dto.live.element.ExplainDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 짧은 단위로 실시간 경기 정보를 바탕으로 데이터를 수정 및 추가
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LiveDataScheduler {

    private final LiveDataService liveDataService;

    @Scheduled(fixedRate = 60_000, initialDelay = 10_000, zone = "Asia/Seoul")
    @SchedulerLock(name = "pollLiveFixturesLock", lockAtMostFor = "55s")
    @Transactional
    @SchedulerLog(action = "liveFixtureScheduling")
    public void pollLiveFixtures() {
        liveDataService.pollLiveFixtures();
    }
}