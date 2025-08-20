package likelion.mlb.backendProject.domain.chat.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AlertRoutingRepository {

  private final EntityManager em;

  /**
   * 해당 선수/경기 이벤트를 받아야 하는 채팅방 목록
   */
  @SuppressWarnings("unchecked")
  public List<UUID> findChatRoomIdsForEvent(UUID playerId, UUID fixtureId) {
    String sql = """
          SELECT DISTINCT cr.id
          FROM fixture f
          JOIN draft d               ON d.round_id = f.round_id
          JOIN participant p         ON p.draft_id = d.id               /* 봇 제외 시 AND p.is_dummy = false */
          JOIN participant_player pp ON pp.participant_id = p.id
          JOIN chat_room cr          ON cr.draft_id = d.id
          WHERE pp.player_id = :playerId
            AND f.id = :fixtureId
        """;
    var rows = em.createNativeQuery(sql)
        .setParameter("playerId", playerId)
        .setParameter("fixtureId", fixtureId)
        .getResultList();
    return ((List<Object>) rows).stream().map(o -> (UUID) o).toList();
  }
}