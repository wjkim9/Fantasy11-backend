package likelion.mlb.backendProject.domain.draft.repository;

import likelion.mlb.backendProject.domain.draft.entity.ParticipantPlayer;

import likelion.mlb.backendProject.domain.match.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ParticipantPlayerRepository extends JpaRepository<ParticipantPlayer, UUID> {

    // 특정 드래프트 방에서 특정 선수가 이미 선택 되었는 지 체크
    boolean existsByParticipant_Draft_IdAndPlayer_Id(UUID draftId, UUID playerId);


    // Participant 엔티티로 드래프트한 선수들 조회
    List<ParticipantPlayer> findByParticipant(Participant participant);

    // Participant ID(UUID)로 드래프트한 선수들 조회
    List<ParticipantPlayer> findByParticipant_Id(UUID participantId);
}
