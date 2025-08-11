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

    // 선택한 선수 pk값
    private UUID playerId;

    // 선택한 선수 영어 이름
    private String playerWebName;

    // 선택한 선수 한글 이름
    private String playerKrName;

    // 선택한 선수 사진
    private String playerPic;

    // 선택한 선수 포지션 pk값
    private UUID elementTypeId;

    // 선택한 선수 포지션 한글명
    private String elementTypeKrName;

    // 선택한 선수 포지션 영어명
    private String elementTypePluralName;

    // 선택한 선수 소속팀 pk값
    private String teamId;

    // 선택한 선수 소속팀 영어 이름
    private String teamName;

    // 선택한 선수 소속팀 한글 이름
    private String teamKrName;

    // 선택한 선수가 이미 선택 되었는 지 여부
    private boolean alreadySelected = false;

}
