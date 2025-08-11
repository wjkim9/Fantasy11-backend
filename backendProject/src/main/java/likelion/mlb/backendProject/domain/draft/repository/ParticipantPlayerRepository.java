package likelion.mlb.backendProject.domain.draft.repository;

import likelion.mlb.backendProject.domain.draft.entity.ParticipantPlayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParticipantPlayerRepository extends JpaRepository<ParticipantPlayer, UUID> {

    // 특정 드래프트 방에서 특정 선수가 이미 선택 되었는 지 체크
    boolean existsByParticipant_Draft_IdAndPlayer_Id(UUID draftId, UUID playerId);
}
