package likelion.mlb.backendProject.domain.match.handler;

import likelion.mlb.backendProject.domain.match.dto.MatchStatusResponse;
import likelion.mlb.backendProject.domain.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchHandler extends TextWebSocketHandler {

    private final MatchService matchService;

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = UUID.randomUUID().toString();
        session.getAttributes().put("userId", userId);
        sessions.add(session); // 💡 꼭 추가해줘야 broadcast에 포함됨

        log.info("🟢 WebSocket 연결됨: {}", session.getId());

        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("{\"type\":\"USER_ID\",\"userId\":\"" + userId + "\"}"));
            } catch (IOException e) {
                log.warn("⛔ 세션 {} 전송 실패 → 제거: {}", session.getId(), e.getMessage());
                sessions.remove(session);
            }
        } else {
            log.warn("❌ 연결은 됐지만 session이 열려있지 않음");
        }
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

        switch (type.toUpperCase()) {
            case "JOIN" -> {
                matchService.joinMatch(userId);
                log.info("JOIN 수신: {} -> Redis 등록", userId);
            }
            case "CANCEL" -> {
                matchService.cancelMatch(userId);
                log.info("CANCEL 수신: {} -> Redis 제거", userId);
                session.close();
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null && matchService.isInMatch(userId)) {
            matchService.cancelMatch(userId);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastStatus() {
        MatchStatusResponse status = matchService.getCurrentStatus();

        JSONObject response = new JSONObject();
        response.put("type", "STATUS");
        response.put("count", status.getCount());
        response.put("remainingTime", status.getRemainingTime());
        response.put("state", status.getState());

        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(response.toString()));
                }
            } catch (IOException e) {
                log.warn("WebSocket 전송 실패: {}", e.getMessage());
                sessions.remove(s);
            }
        }
    }
}
