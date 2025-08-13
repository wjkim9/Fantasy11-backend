package likelion.mlb.backendProject.domain.draft.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftResponse {

    private UUID participantId; // 드래프트 참가자 pk값

    private UUID playerId; // player pk값

    private String PlayerWebName; // player 영어 이름

    private String PlayerKrName; // player 한글 이름

    private String PlayerPic; // player 사진

    private String teamName; // 소속팀 영어명

    private String teamKrName; // 소속팀 한글명

    private String elementTypePluralName; // 포지션 영어명

    private String elementTypeKrName; // 포지션 한글명

    // 선택한 선수가 이미 선택 되었는 지 여부
    private boolean alreadySelected = false;

}
