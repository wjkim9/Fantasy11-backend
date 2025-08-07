package likelion.mlb.backendProject.domain.match.service;

import likelion.mlb.backendProject.domain.match.dto.RoundInfo;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.staticdata.entity.Round;
import likelion.mlb.backendProject.global.staticdata.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
@RequiredArgsConstructor
public class DraftTimingService {

    private final RoundRepository roundRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public RoundInfo getNextDraftWindowOrThrow() {
        LocalDateTime nowKST = LocalDateTime.now(KST);

        // ✅ UTC 기준으로 변환한 후, Round 중 가장 가까운 미래를 가져옴
        OffsetDateTime nowUTC = nowKST.atZone(KST).toOffsetDateTime();
        Round r = roundRepository.findFirstByStartedAtAfterOrderByStartedAtAsc(nowUTC);

        if (r == null || r.getStartedAt() == null) {
            throw BaseException.ROUND_NOT_FOUND;
        }

        // ✅ OffsetDateTime → KST LocalDateTime 변환
        LocalDateTime startedAtKST = r.getStartedAt().atZoneSameInstant(KST).toLocalDateTime();

        LocalDate draftDay = startedAtKST.toLocalDate().minusDays(2);
        LocalDateTime openAt = LocalDateTime.of(draftDay, LocalTime.of(10, 30));
        LocalDateTime lockAt = LocalDateTime.of(draftDay, LocalTime.of(12, 30));

        return new RoundInfo(
                r.getId(),
                r.getRound(),
                openAt.toString(),
                lockAt.toString()
        );
    }
}