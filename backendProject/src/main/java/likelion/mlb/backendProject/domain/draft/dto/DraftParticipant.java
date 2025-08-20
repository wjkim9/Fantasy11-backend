package likelion.mlb.backendProject.domain.draft.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


/*
* 드래프트 방에 참여한 참여자 리스트 가져오는 dto
* */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftParticipant {

    private UUID participantId; // participant pk값

    private short participantUserNumber; // 드래프트 방에서 참여자 순서

    private boolean participantDummy; // 더미 여부(봇 여부)

    private String userEmail; // 이메일

    private String userName; // 참여자명

    private boolean userFlag = false; // 사용자 본인 여부

}
