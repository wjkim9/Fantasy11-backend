package likelion.mlb.backendProject.domain.match.scheduler;

import likelion.mlb.backendProject.domain.match.service.DraftTimingService;
import likelion.mlb.backendProject.domain.match.service.MatchTimeoutProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MatchScheduler {
    private final DraftTimingService draftTimingService;
    private final MatchTimeoutProcessor processor;

    private volatile UUID processedRoundId;

    @Scheduled(cron = "*/5 * * * * *") // 5초마다 체크
    public void tick() {
        var round = draftTimingService.getNextDraftWindowOrThrow();
        var lockAt = LocalDateTime.parse(round.getLockAt());
        if (LocalDateTime.now(ZoneId.of("Asia/Seoul")).isAfter(lockAt)
                && !round.getId().equals(processedRoundId)) {
            var map = processor.processTimeoutAndInsert();
            processedRoundId = round.getId();
        }
    }
}
