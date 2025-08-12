package likelion.mlb.backendProject.global.scheduler;


import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.domain.draft.repository.ParticipantPlayerRepository;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.round.entity.Round;
import likelion.mlb.backendProject.domain.round.entity.RoundScore;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundScoreRepository;
import likelion.mlb.backendProject.global.scheduler.service.RoundSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoundSettlementScheduler {

    private final RoundSettlementService roundSettlementService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "settleRoundsLock", lockAtMostFor = "5m", lockAtLeastFor = "10s")
    @Transactional
    public void run() {
        roundSettlementService.settle();
    }

}
