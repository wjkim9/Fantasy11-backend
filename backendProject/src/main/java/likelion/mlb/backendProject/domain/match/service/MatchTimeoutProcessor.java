package likelion.mlb.backendProject.domain.match.service;

import jakarta.transaction.Transactional;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomService;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.domain.match.dto.AssignDto;
import likelion.mlb.backendProject.domain.match.dto.RoundInfo;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.domain.round.entity.Round;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.user.entity.User;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchTimeoutProcessor {

    private final RedisTemplate<String, String> redisTemplate;
    private final DraftRepository draftRepository;
    private final ParticipantRepository participantRepository;
    private final RoundRepository roundRepository;
    private final DraftTimingService draftTimingService;
    private final ChatRoomService chatRoomService;

    private static final String SESSION_KEY = "match:session";

    /**
     * 타임아웃 시점: 대기중 유저를 4인 그룹으로 나눠 Draft/Participant 저장하고,
     * 사용된 실제 유저는 Redis SET에서 제거.
     * 반환: 실제 유저에게 전달할 (userId -> draftId, userNumber) 매핑
     */
    @Transactional
    public Map<String, AssignDto> processTimeoutAndInsert() {
        // 1) 현재 대기 인원 스냅샷
        Set<String> members = redisTemplate.opsForSet().members(SESSION_KEY);
        if (members == null || members.isEmpty()) return Map.of();

        // 공정성 위해 셔플
        List<String> users = new ArrayList<>(members);
        Collections.shuffle(users);

        // 2) 4인씩 그룹핑 + 마지막 그룹 패딩
        List<List<String>> groups = splitBySize(users, 4);
        if (!groups.isEmpty()) {
            List<String> last = groups.get(groups.size() - 1);
            if (last.size() < 4) padWithDummies(last);
        }

        // 3) 라운드 조회
        RoundInfo roundInfo = draftTimingService.getNextDraftWindowOrThrow();
        UUID roundId = roundInfo.getId();
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new BaseException(ErrorCode.ROUND_NOT_FOUND));

        Map<String, AssignDto> result = new HashMap<>();
        List<String> usedRealUserIds = new ArrayList<>();

        // 4) 그룹별 Draft/Participant INSERT
        for (List<String> group : groups) {
            if (group.size() != 4) continue; // 방은 항상 4명

            Draft draft = new Draft();
            draft.setId(UUID.randomUUID());
            draft.setRound(round);
            // draft.setDeleted(false); // ← is_deleted NOT NULL이고 default 없다면 주석 해제
            draftRepository.save(draft);

            // 필요 시 채팅방 생성
            chatRoomService.createRoom(draft.getId());

            short userNumber = 1;
            List<Participant> participants = new ArrayList<>(4);

            for (String uid : group) {
                Participant p = new Participant();
                p.setId(UUID.randomUUID());
                p.setDraft(draft);
                p.setUserNumber(userNumber++);

                if (uid.startsWith("DUMMY:")) {
                    p.setDummy(true);
                    p.setUser(null);
                } else {
                    p.setDummy(false);
                    User userRef = new User(); // 프록시 참조만 세팅
                    userRef.setId(UUID.fromString(uid));
                    p.setUser(userRef);

                    // 결과 매핑
                    result.put(uid, new AssignDto(draft.getId(), (short) (p.getUserNumber())));
                    usedRealUserIds.add(uid);
                }
                participants.add(p);
            }
            participantRepository.saveAll(participants);
        }

        // 5) 사용된 실제 유저만 Redis에서 제거
        if (!usedRealUserIds.isEmpty()) {
            redisTemplate.opsForSet().remove(SESSION_KEY, usedRealUserIds.toArray());
        }

        return result;
    }

    /** n개씩 분할 */
    private static <T> List<List<T>> splitBySize(List<T> list, int n) {
        List<List<T>> res = new ArrayList<>();
        for (int i = 0; i < list.size(); i += n) {
            res.add(new ArrayList<>(list.subList(i, Math.min(i + n, list.size()))));
        }
        return res;
    }

    /** 4명이 될 때까지 더미 추가 */
    private static void padWithDummies(List<String> group) {
        int need = 4 - group.size();
        for (int i = 0; i < need; i++) {
            group.add("DUMMY:" + UUID.randomUUID());
        }
    }
}
