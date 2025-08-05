package likelion.mlb.backendProject.domain.draft.handler;

import java.security.Principal;
import java.util.UUID;

/**
 *  Principal  <- 사용자 인증 정보 구현체
 *  사용자 고유 식별자(memberPk)를 반환
 **/
public class StompPrincipal implements Principal {

    private final UUID participantId;

    public StompPrincipal(UUID participantId) {
        this.participantId = participantId;
    }

    public UUID getParticipantId() {
        return this.participantId;
    }

    @Override
    public String getName() {
        return String.valueOf(participantId);
    }
}
