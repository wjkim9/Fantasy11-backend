package likelion.mlb.backendProject.domain.draft.service;

import likelion.mlb.backendProject.domain.draft.dto.DraftRequest;
import likelion.mlb.backendProject.domain.draft.dto.StartTurnRequest;
import likelion.mlb.backendProject.domain.draft.dto.TurnSnapshot;
import likelion.mlb.backendProject.domain.draft.util.DraftKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TurnService {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messaging;

    /**
     * 현재 턴 시작/갱신:
     * - Redis 해시에 currentParticipantId, roundNo, deadlineAt 저장
     * - 타이머 키에 TTL 설정
     * - STOMP로 스냅샷 즉시 브로드캐스트
     */
    public void startOrUpdateTurn(UUID roomId, StartTurnRequest req) {
        String stateKey = DraftKeys.state(roomId);
        String timerKey = DraftKeys.timer(roomId);

        int winSec = (req.pickWindowSec() == null) ? 60 : req.pickWindowSec();
        long now = Instant.now().toEpochMilli();
        long deadlineAt = now + winSec * 1000L;

        Map<String, String> m = new HashMap<>();
        m.put("currentParticipantId", req.currentParticipantId().toString());
        m.put("roundNo", String.valueOf(req.roundNo()));
        m.put("deadlineAt", String.valueOf(deadlineAt));
        m.put("updatedAt", String.valueOf(now));
        m.put("draftCnt", String.valueOf(req.draftCnt()));
        redis.opsForHash().putAll(stateKey, m);

        // 타이머 TTL: 키 값은 검증/디버깅용으로 deadlineAt 저장(원하는 값으로 대체 가능)
//        redis.opsForValue().set(timerKey, String.valueOf(deadlineAt), winSec, TimeUnit.SECONDS);

        // 바로 한 번 쏘기
//        broadcastTurnOnce(roomId);
    }

    /**
     * Redis 상태를 읽어 한 번 브로드캐스트 (누구 턴/몇 라운드/남은 초)
     */
    public void broadcastTurnOnce(UUID roomId) {
        String stateKey = DraftKeys.state(roomId);
        String timerKey = DraftKeys.timer(roomId);

        var vals = redis.opsForHash().multiGet(stateKey, Arrays.asList("currentParticipantId", "roundNo", "deadlineAt"));
        if (vals == null || vals.get(0) == null || vals.get(1) == null || vals.get(2) == null) return;

        String currentParticipantId = (String) vals.get(0);
        int roundNo = Integer.parseInt((String) vals.get(1));
        long deadlineAt = Long.parseLong((String) vals.get(2));

        // 남은 초 (deadline 기준)
        long now = Instant.now().toEpochMilli();
        int remainingByDeadline = (int) Math.max(0, (deadlineAt - now) / 1000);

        // 보정: timer TTL이 있으면 TTL 우선
        Long ttl = redis.getExpire(timerKey, TimeUnit.SECONDS); // -2 없음, -1 TTL 없음
        int remainingSec = (ttl != null && ttl >= 0) ? ttl.intValue() : remainingByDeadline;

        TurnSnapshot payload = new TurnSnapshot(
                "TURN_SNAPSHOT", roomId, currentParticipantId, roundNo, remainingSec, deadlineAt
        );
        messaging.convertAndSend(DraftKeys.topic(roomId), payload);
    }

    public boolean checkCurrentParticipant(DraftRequest draftRequest) {
        UUID draftId = draftRequest.getDraftId();
        UUID participantId = draftRequest.getParticipantId();

        String stateKey = DraftKeys.state(draftId);
//        String timerKey = DraftKeys.timer(draftId);


        var vals = redis.opsForHash().multiGet(stateKey, Arrays.asList("currentParticipantId"));
        if ( vals == null || vals.get(0) == null || ((String)vals.get(0)).trim().equals("") ) return false;

        UUID currentParticipantId = UUID.fromString((String)vals.get(0));

        if(participantId.equals(currentParticipantId)) {
            return true;
        } else {
            return false;
        }
    }

    public Integer getDraftCnt(UUID draftID) {

        String stateKey = DraftKeys.state(draftID);
        var vals = redis.opsForHash().multiGet(stateKey, Arrays.asList("draftCnt"));
        if (vals == null || vals.get(0) == null || ((String)vals.get(0)).trim().equals("")
        || ((String)vals.get(0)).trim().equals("null")) return 0;


        return Integer.parseInt((String)vals.get(0));
    }
}