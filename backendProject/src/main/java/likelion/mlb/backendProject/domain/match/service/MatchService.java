package likelion.mlb.backendProject.domain.match.service;

import likelion.mlb.backendProject.domain.match.dto.MatchStatusResponse;
import likelion.mlb.backendProject.domain.match.dto.RoundInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DraftTimingService draftTimingService;

    private static final String SESSION_KEY = "match:session";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public MatchStatusResponse getCurrentStatus() {
        long userCount = Optional.ofNullable(redisTemplate.opsForSet().size(SESSION_KEY)).orElse(0L);

        RoundInfo round = draftTimingService.getNextDraftWindowOrThrow();
        LocalDateTime now = LocalDateTime.now(KST);

        LocalDateTime openAt = LocalDateTime.parse(round.getOpenAt());
        LocalDateTime lockAt = LocalDateTime.parse(round.getLockAt());

        String state;
        String remaining;

        if (now.isBefore(openAt)) {
            state = "BEFORE_OPEN";
            remaining = formatRemaining(now, openAt);
        } else if (!now.isAfter(lockAt)) {
            state = "OPEN";
            remaining = formatRemaining(now, lockAt);
        } else {
            state = "LOCKED";
            remaining = "00:00";
        }

        return new MatchStatusResponse(userCount, state, remaining, round);
    }

    private String formatRemaining(LocalDateTime now, LocalDateTime target) {
        Duration duration = Duration.between(now, target);
        if (duration.isNegative()) return "00:00";

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void joinMatch(String userId) {
        redisTemplate.opsForSet().add("match:session", userId);
    }

    public void cancelMatch(String userId) {
        redisTemplate.opsForSet().remove("match:session", userId);
    }

    public boolean isInMatch(String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember("match:session", userId));
    }
}

