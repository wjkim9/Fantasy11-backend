package likelion.mlb.backendProject.domain.chat.repository;

import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatMembershipRepository {

  private final EntityManager em;

  /** roomId + userId가 같은 드래프트의 참가자인가? (봇 포함) */
  public boolean isMember(UUID roomId, UUID userId) {
    String sql = """
      SELECT 1
        FROM chat_room cr
        JOIN participant p ON p.draft_id = cr.draft_id
       WHERE cr.id = :roomId
         AND p.user_id = :userId
       LIMIT 1
      """;
    Query q = em.createNativeQuery(sql)
        .setParameter("roomId", roomId)
        .setParameter("userId", userId);
    return !q.getResultList().isEmpty();
  }
}
