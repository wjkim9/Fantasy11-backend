package likelion.mlb.backendProject.domain.draft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftRequest {

    private UUID draftId; // 드래프트 방 pk값

    private UUID participantId; // 드래프트 참가자 pk값

    private String userName; // 참가자 이름

    private UUID playerId; // player pk값

    private String playerWebName; // player 영어 이름

    private String playerKrName; // player 한글 이름

    private String playerPic; // player 사진

    private String teamName; // 소속팀 영어명

    private String teamKrName; // 소속팀 한글명

    private UUID elementTypeId; // 포지션 pk값

    private String elementTypePluralName; // 포지션 영어명

    private String elementTypeKrName; // 포지션 한글명

    // 선택한 선수가 이미 선택 되었는 지 여부
    private boolean alreadySelected = false;

    // 한 참가자가 선수를 드래프트 했을 시 포지션 별 최대/최소 값 유지하는 지 여부
    private boolean isWithinSquadLimits = true;

}
