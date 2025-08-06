package likelion.mlb.backendProject.domain.match.service;

import likelion.mlb.backendProject.domain.match.dto.DraftWindow;
import likelion.mlb.backendProject.domain.match.dto.MatchStatusResponse;
import likelion.mlb.backendProject.domain.match.dto.MatchUuidResponse;
import likelion.mlb.backendProject.domain.match.dto.RoundInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final RedisTemplate<String, String> redisTemplate;
    private final DraftTimingService draftTimingService;

    private static final String SESSION_KEY = "match:session";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public MatchStatusResponse getCurrentStatus() {
        Long count = redisTemplate.opsForSet().size(SESSION_KEY);
        long userCount = count == null ? 0 : count;

        DraftWindow draftWindow = draftTimingService.getNextDraftWindowOrThrow();
        LocalDateTime now = LocalDateTime.now(KST);

        String state;
        String remaining;

        if (now.isBefore(draftWindow.getOpenAt())) {
            state = "BEFORE_OPEN";
            remaining = formatRemaining(now, draftWindow.getOpenAt());
        } else if (!now.isAfter(draftWindow.getLockAt())) {
            state = "OPEN";
            remaining = formatRemaining(now, draftWindow.getLockAt());
        } else {
            state = "LOCKED";
            remaining = "00:00";
        }

        return new MatchStatusResponse(
                userCount,
                state,
                remaining,
                new RoundInfo(
                        draftWindow.getRoundId(),
                        draftWindow.getRoundNo(),
                        draftWindow.getOpenAt().toString(),
                        draftWindow.getLockAt().toString()
                )
        );
    }

    public MatchUuidResponse generateUserId() {
        String uuid = UUID.randomUUID().toString();
        Long count = redisTemplate.opsForSet().size(SESSION_KEY);
        return new MatchUuidResponse(uuid, count == null ? 0 : count);
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

