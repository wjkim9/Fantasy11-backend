package likelion.mlb.backendProject.global.scheduler;

import likelion.mlb.backendProject.global.aop.SchedulerLog;
import likelion.mlb.backendProject.global.scheduler.service.LiveDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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