package likelion.mlb.backendProject.domain.draft.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftRequest {

    // 드래프트 방 pk값
    private UUID draftId;

    // 드래프트 참가자 pk값
    private UUID participantId;

    // 선수 pk값
    private UUID playerId;
}
