package likelion.mlb.backendProject.global.scheduler;

import likelion.mlb.backendProject.global.aop.SchedulerLog;
import likelion.mlb.backendProject.global.scheduler.service.DataUpdaterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 한국 시간 기준 매일 오전 8시에 라운드 시간, 경기 시간, 팀, 선수를 조회해서 변경사항이 있으면 변경한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataUpdaterScheduler {

    private final DataUpdaterService dataUpdaterService;

    //매일 오전 8시에 실행
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    @Transactional
    @SchedulerLog(action = "dailyScheduling")
    public void run() {
        dataUpdaterService.fullRefresh();
    }
}
