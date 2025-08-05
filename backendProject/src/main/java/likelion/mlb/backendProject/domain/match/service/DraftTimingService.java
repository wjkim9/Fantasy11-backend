package likelion.mlb.backendProject.domain.match.service;

import likelion.mlb.backendProject.domain.match.dto.DraftWindow;
import likelion.mlb.backendProject.global.exception.RoundNotFoundException;
import likelion.mlb.backendProject.global.staticdata.entity.Round;
import likelion.mlb.backendProject.global.staticdata.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DraftTimingService {

    private final RoundRepository roundRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public DraftWindow getNextDraftWindowOrThrow() {
        LocalDateTime now = LocalDateTime.now(KST);
        Round r = roundRepository.findFirstByStartedAtAfterOrderByStartedAtAsc(now);
        if (r == null || r.getStartedAt() == null) {
            throw new RoundNotFoundException("No upcoming round with started_at.");
        }

        LocalDate d2 = r.getStartedAt().toLocalDate().minusDays(2);
        LocalDateTime openAt = LocalDateTime.of(d2, LocalTime.of(15, 30));
        LocalDateTime lockAt = LocalDateTime.of(d2, LocalTime.of(15, 31));

        return new DraftWindow(r.getId(), r.getRound(), openAt, lockAt);
    }
}
