package likelion.mlb.backendProject.domain.match.controller;

import likelion.mlb.backendProject.domain.match.dto.DraftWindow;
import likelion.mlb.backendProject.domain.match.dto.MatchStatusResponse;
import likelion.mlb.backendProject.domain.match.dto.MatchUuidResponse;
import likelion.mlb.backendProject.domain.match.service.DraftTimingService;
import likelion.mlb.backendProject.global.exception.RoundNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchRestController {

    private final RedisTemplate<String, String> redisTemplate;
    private final DraftTimingService draftTimingService;

    private static final String SESSION_KEY = "match:session";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @GetMapping("/status")
    public ResponseEntity<MatchStatusResponse> getStatus() {
        Long count = redisTemplate.opsForSet().size(SESSION_KEY);
        long userCount = count == null ? 0 : count;

        try {
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

            MatchStatusResponse response = new MatchStatusResponse(
                    userCount,
                    state,
                    remaining,
                    draftWindow.getRoundId(),
                    draftWindow.getRoundNo(),
                    draftWindow.getOpenAt().toString(),
                    draftWindow.getLockAt().toString()
            );

            return ResponseEntity.ok(response);

        } catch (RoundNotFoundException e) {
            MatchStatusResponse response = new MatchStatusResponse(
                    userCount,
                    "NO_ROUND",
                    "--:--",
                    null,
                    0,
                    null,
                    null
            );
            return ResponseEntity.ok(response);
        }
    }


    private String formatRemaining(LocalDateTime now, LocalDateTime target) {
        Duration duration = Duration.between(now, target);
        if (duration.isNegative()) return "00:00";

        long minutes = duration.toMinutes();
        long seconds = duration.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }

    @GetMapping("/uuid")
    public ResponseEntity<MatchUuidResponse> generateUserId() {
        String uuid = UUID.randomUUID().toString();
        Long count = redisTemplate.opsForSet().size(SESSION_KEY);
        return ResponseEntity.ok(new MatchUuidResponse(uuid, count == null ? 0 : count));
    }

}
