package likelion.mlb.backendProject.domain.draft.dto;

import java.util.UUID;

public record StartTurnRequest(
        UUID currentParticipantId,
        Integer roundNo,        // 1..11
        Integer pickWindowSec,   // null이면 기본 60초
        Integer draftCnt // 해당 드래프트 룸에서 현재까지 몇 명 드래프트 했는지
) {}