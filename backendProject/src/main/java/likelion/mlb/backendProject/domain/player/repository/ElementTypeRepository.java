package likelion.mlb.backendProject.domain.player.repository;

import likelion.mlb.backendProject.domain.player.entity.ElementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ElementTypeRepository extends JpaRepository<ElementType, UUID> {

    /*
    * 한 참가자(participant)가 선택할 수 있는 선수 포지션 가져오기
    * */
    @Query("""
        SELECT e
        FROM ElementType e
        LEFT JOIN Player p ON p.elementType = e
        LEFT JOIN ParticipantPlayer pp ON pp.player = p AND pp.participant.id = :participantId
        GROUP BY e.id, e.squadMaxPlay
        HAVING COALESCE(COUNT(pp.id), 0) < e.squadMaxPlay
    """)
    List<ElementType> findAvailableElementTypesByParticipant(@Param("participantId") UUID participantId);
}
