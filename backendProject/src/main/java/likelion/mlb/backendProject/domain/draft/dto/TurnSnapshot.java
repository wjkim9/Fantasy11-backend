package likelion.mlb.backendProject.domain.draft.dto;

import java.util.UUID;

public record TurnSnapshot(
        String type,        // "TURN_SNAPSHOT"
        UUID roomId,
        String currentParticipantId,
        int roundNo,        // 1..11
        int remainingSec,   // 서버 계산 남은 초
        long deadlineAt     // epoch ms (클라가 보정용으로 사용)
) {}