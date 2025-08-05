package likelion.mlb.backendProject.domain.match.handler;

import likelion.mlb.backendProject.domain.match.dto.DraftWindow;
import likelion.mlb.backendProject.domain.match.service.DraftTimingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchHandler extends TextWebSocketHandler {

    private final RedisTemplate<String, String> redisTemplate;
    private final DraftTimingService draftTimingService; // 추가됨

    private static final String MATCH_SESSION_KEY = "match:session";
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        String userId = UUID.randomUUID().toString();
        session.getAttributes().put("userId", userId);
        log.info("연결됨: {}", userId);

        // 👉 Redis에는 등록하지 않음 (JOIN 메시지에서만)
        session.sendMessage(new TextMessage("{\"type\":\"USER_ID\",\"userId\":\"" + userId + "\"}"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JSONObject json = new JSONObject(message.getPayload());
        String type = json.optString("type", "");

        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("세션에 userId 없음. 연결 종료");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        if ("JOIN".equalsIgnoreCase(type)) {
            redisTemplate.opsForSet().add(MATCH_SESSION_KEY, userId);
            log.info("JOIN 수신: {} -> Redis 등록", userId);
        }

        if ("CANCEL".equalsIgnoreCase(type)) {
            redisTemplate.opsForSet().remove(MATCH_SESSION_KEY, userId);
            log.info("CANCEL 수신: {} -> Redis 제거", userId);
            session.close();
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null && redisTemplate.opsForSet().isMember(MATCH_SESSION_KEY, userId)) {
            redisTemplate.opsForSet().remove(MATCH_SESSION_KEY, userId);
        }
    }

    // ✅ 1초마다 대기 인원 + 남은 시간 broadcast
    @Scheduled(fixedRate = 1000)
    public void broadcastStatus() {
        Long count = redisTemplate.opsForSet().size(MATCH_SESSION_KEY);

        String state = "NO_ROUND";
        String remainingTime = "--:--";

        try {
            DraftWindow draftWindow = draftTimingService.getNextDraftWindowOrThrow();
            LocalDateTime now = LocalDateTime.now(KST);

            if (now.isBefore(draftWindow.getOpenAt())) {
                state = "BEFORE_OPEN";
                remainingTime = formatRemaining(now, draftWindow.getOpenAt());
            } else if (!now.isAfter(draftWindow.getLockAt())) {
                state = "OPEN";
                remainingTime = formatRemaining(now, draftWindow.getLockAt());
            } else {
                state = "LOCKED";
                remainingTime = "00:00";
            }
        } catch (Exception e) {
            // NO_ROUND 유지
        }

        JSONObject response = new JSONObject();
        response.put("type", "STATUS");
        response.put("count", count == null ? 0 : count);
        response.put("remainingTime", remainingTime);
        response.put("state", state);

        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(response.toString()));
                }
            } catch (IOException e) {
                log.warn("WebSocket 전송 실패: {}", e.getMessage());
            }
        }
    }

    private String formatRemaining(LocalDateTime now, LocalDateTime target) {
        Duration d = Duration.between(now, target);
        if (d.isNegative()) return "00:00";
        long minutes = d.toMinutes();
        long seconds = d.minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d", minutes, seconds);
    }
}

