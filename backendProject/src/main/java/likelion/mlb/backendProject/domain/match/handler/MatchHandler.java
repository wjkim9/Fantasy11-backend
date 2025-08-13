package likelion.mlb.backendProject.domain.match.handler;

import likelion.mlb.backendProject.domain.match.dto.AssignDto;
import likelion.mlb.backendProject.domain.match.service.MatchService;
import likelion.mlb.backendProject.domain.match.service.MatchTimeoutProcessor;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchHandler extends TextWebSocketHandler {

    private final MatchService matchService;
    private final MatchTimeoutProcessor matchTimeoutProcessor;

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final Map<String, WebSocketSession> latestByUser = new ConcurrentHashMap<>();

    // 라운드별 타임아웃 배치 중복 실행 방지용
    private final AtomicReference<String> lastProcessedRoundId = new AtomicReference<>(null);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var principal = session.getPrincipal();
        if (!(principal instanceof Authentication auth) || !auth.isAuthenticated()) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        var cud = (CustomUserDetails) auth.getPrincipal();
        String userId = cud.getUser().getId().toString();

        // 기존 세션 있으면 닫기 (유저당 1세션 유지)
        WebSocketSession prev = latestByUser.put(userId, session);
        if (prev != null && prev.isOpen() && prev != session) {
            try { prev.close(CloseStatus.NORMAL); } catch (Exception ignored) {}
        }

        session.getAttributes().put("userId", userId);
        sessions.add(session);
        log.info("🟢 WebSocket 연결됨: {} (userId={})", session.getId(), userId);

        try {
            // 1) USER_ID
            session.sendMessage(new TextMessage("{\"type\":\"USER_ID\",\"userId\":\"" + userId + "\"}"));

            // 2) STATUS 즉시 전송
            var status = matchService.getCurrentStatus();
            var resp = new JSONObject();
            resp.put("type", "STATUS");
            resp.put("count", status.getCount());
            resp.put("remainingTime", status.getRemainingTime());
            resp.put("state", status.getState());
            var round = status.getRound();
            if (round != null) {
                var r = new JSONObject();
                r.put("id", round.getId().toString());
                r.put("no", round.getNo());
                r.put("openAt", round.getOpenAt());
                r.put("lockAt", round.getLockAt());
                resp.put("round", r);
            }
            session.sendMessage(new TextMessage(resp.toString()));
        } catch (IOException ex) {
            log.warn("초기 메시지 전송 중 끊김: {}", ex.getMessage());
        }
    }

    private String extractUserId(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal instanceof org.springframework.security.core.Authentication auth) {
            Object p = auth.getPrincipal();
            if (p instanceof CustomUserDetails cud) {
                return cud.getUser().getId().toString(); // UUID
            }
        }
        return null;
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
        if (userId != null) {
            // latestByUser 최신세션만 정리
            latestByUser.compute(userId, (k, v) -> (v == session) ? null : v);

            // ❌ 자동 취소 제거: 재연결/탭 이동 중에도 대기열 유지
            // if (matchService.isInMatch(userId)) {
            //     matchService.cancelMatch(userId);
            // }
        }
    }


    @Scheduled(fixedRate = 1000)
    public void broadcastStatus() {
        var status = matchService.getCurrentStatus();

        // 1) 상태 브로드캐스트 (세션별 LOCKED_HOLD 처리)
        for (WebSocketSession s : sessions) {
            try {
                if (!s.isOpen()) continue;
                String uid = (String) s.getAttributes().get("userId");

                var msg = new JSONObject();
                msg.put("type", "STATUS");
                msg.put("count", status.getCount());
                msg.put("remainingTime", status.getRemainingTime());

                String state = status.getState();
                if ("LOCKED".equals(state) && uid != null && matchService.isInMatch(uid)) {
                    msg.put("state", "LOCKED_HOLD");
                } else {
                    msg.put("state", state);
                }

                var round = status.getRound();
                if (round != null) {
                    var r = new JSONObject();
                    r.put("id", round.getId().toString());
                    r.put("no", round.getNo());
                    r.put("openAt", round.getOpenAt());
                    r.put("lockAt", round.getLockAt());
                    msg.put("round", r);
                }

                s.sendMessage(new TextMessage(msg.toString()));
            } catch (IOException e) {
                log.warn("WebSocket 전송 실패: {}", e.getMessage());
                sessions.remove(s);
            }
        }

        // 2) LOCKED로 넘어간 첫 틱에만 타임아웃 배치 실행 + DRAFT_START 푸시
        var round = status.getRound();
        if ("LOCKED".equals(status.getState()) && round != null) {
            String rid = round.getId().toString();
            String prev = lastProcessedRoundId.get();
            if (prev == null || !prev.equals(rid)) {
                // 동시성 가드 (여러 스레드가 들어오더라도 최초 1회만)
                if (lastProcessedRoundId.compareAndSet(prev, rid)) {
                    try {
                        log.info("⏰ LOCKED 진입: roundId={} → 타임아웃 배치 실행", rid);

                        // DB 반영 (Draft/Participant 생성, Redis 정리)
                        Map<String, AssignDto> assignments = matchTimeoutProcessor.processTimeoutAndInsert();

                        log.info("🧩 배정 완료: {} 명", assignments.size());

                        // 개인 푸시
                        assignments.forEach((uid, a) -> {
                            WebSocketSession target = latestByUser.get(uid);
                            if (target != null && target.isOpen()) {
                                var msg = new JSONObject();
                                msg.put("type", "DRAFT_START");
                                msg.put("draftId", a.getDraftId().toString());
                                msg.put("userNumber", a.getUserNumber());
                                try {
                                    target.sendMessage(new TextMessage(msg.toString()));
                                } catch (Exception e) {
                                    log.warn("DRAFT_START 전송 실패 userId={}: {}", uid, e.getMessage());
                                }
                            }
                        });
                    } catch (Exception e) {
                        log.error("타임아웃 배치 실행 실패: {}", e.getMessage(), e);
                        // 실패 시 다시 시도하게 하려면 가드 해제
                        lastProcessedRoundId.set(prev);
                    }
                }
            }
        }
    }

    public void notifyDraftStart(String userId, UUID draftId, short userNumber) {
        WebSocketSession s = latestByUser.get(userId);
        if (s != null && s.isOpen()) {
            var msg = new JSONObject();
            msg.put("type", "DRAFT_START");
            msg.put("draftId", draftId.toString());
            msg.put("userNumber", userNumber);
            try {
                s.sendMessage(new TextMessage(msg.toString()));
            } catch (IOException e) {
                log.warn("DRAFT_START 전송 실패: {}", e.getMessage());
            }
        }
    }
}
