package likelion.mlb.backendProject.global.scheduler.service;

import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.domain.draft.repository.ParticipantPlayerRepository;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.round.entity.Round;
import likelion.mlb.backendProject.domain.round.entity.RoundScore;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoundSettlementService {

    private final RoundRepository roundRepository;
    private final DraftRepository draftRepository;
    private final ParticipantPlayerRepository participantPlayerRepository;
    private final RoundScoreRepository roundScoreRepository;

    public void settle() {

        // 1) 끝났지만 아직 정산 안 된 라운드
        List<Round> targets = roundRepository.findUnsettledFinishedRounds();
        if (targets.isEmpty()) return;

        for (Round round : targets) {
            settleOneRound(round);
            // 5) 재실행 방지 플래그
            round.changeSettled();
        }
    }

    private void settleOneRound(Round round) {
        // 2) 해당 라운드의 드래프트 방 + 참가자 로드 (N+1 방지용 fetch join)
        List<Draft> drafts = draftRepository.findByRoundFetchParticipants(round);
        if (drafts.isEmpty()) return;

        for (Draft draft : drafts) {
            List<Participant> participants = draft.getParticipants();
            if (participants.isEmpty()) continue; // 혹시 모를 예외

            // 3) 참가자별 라운드 총점 집계 (선택한 선수들의 player_fixture_stats.total_points 합계)
            Map<UUID, Integer> sumByParticipant =
                    sumRoundPointsByParticipant(round, participants);

            // ===== 랭킹/승점 부여 (동점 처리 + 0점은 무조건 4등) 시작 =====

            // (없으면 0점 취급)
            participants.forEach(p -> sumByParticipant.putIfAbsent(p.getId(), 0));

            // 참가자/총점 목록을 0점/양수로 분리
            List<Participant> positives = new ArrayList<>();
            List<Participant> zeros = new ArrayList<>();
            for (Participant p : participants) {
                int total = sumByParticipant.getOrDefault(p.getId(), 0);
                if (total > 0) positives.add(p); else zeros.add(p);
            }

            // 양수 그룹 내림차순 정렬
            positives.sort((a, b) -> Integer.compare(
                    sumByParticipant.getOrDefault(b.getId(), 0),
                    sumByParticipant.getOrDefault(a.getId(), 0)
            ));

            // competition ranking: 1,1,3,4 방식으로 공동 순위 부여
            Map<UUID, Integer> rankByParticipant = new HashMap<>();
            int processed = 0;
            Integer prevScore = null;
            int currentRank = 0;

            for (Participant p : positives) {
                int score = sumByParticipant.getOrDefault(p.getId(), 0);
                if (prevScore == null || !prevScore.equals(score)) {
                    currentRank = processed + 1; // 새 점수면 현재까지 인원 수 + 1
                    prevScore = score;
                }
                rankByParticipant.put(p.getId(), currentRank);
                processed++;
            }

            // 0점 참가자는 전부 4등 고정
            for (Participant p : zeros) {
                rankByParticipant.put(p.getId(), 4);
            }

            // 순위 -> 승점 매핑 (1등3, 2등2, 3등1, 4등0)
            int[] winPts = {3, 2, 1, 0};

            for (Participant p : participants) {
                int totalPoints = sumByParticipant.getOrDefault(p.getId(), 0);
                int rank = rankByParticipant.getOrDefault(p.getId(), 4);
                int leaguePoints = (rank >= 1 && rank <= 4) ? winPts[rank - 1] : 0;

                // 멱등성: 같은 (user_id, round_id) 는 한 번만 저장되게 UNIQUE 제약
                if (!roundScoreRepository.existsByUserIdAndRoundId(p.getUser().getId(), round.getId())) {
                    RoundScore rs = RoundScore.RoundScoreBuilder(
                            leaguePoints,
                            totalPoints,
                            p.getUser(),
                            round
                    );
                    roundScoreRepository.save(rs);
                }

                // 방 내 결과 컬럼 업데이트 (순위/승점 저장 등)
                p.updateRank(leaguePoints, rank);
            }
            // ===== 랭킹/승점 부여 끝 =====
        }
    }

    private Map<UUID, Integer> sumRoundPointsByParticipant(Round round, List<Participant> participants) {
        // JPQL 한방 집계 (아래 Repository 쿼리 참고)
        List<Object[]> rows = participantPlayerRepository.sumPointsByParticipant(round, participants);
        Map<UUID, Integer> map = new HashMap<>();
        for (Object[] r : rows) {
            UUID participantId = (UUID) r[0];
            Integer sum = ((Number) r[1]).intValue();
            map.put(participantId, sum);
        }
        return map;
    }
}
