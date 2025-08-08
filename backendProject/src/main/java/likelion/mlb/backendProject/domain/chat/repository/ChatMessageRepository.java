package likelion.mlb.backendProject.domain.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  @Query("""
      SELECT m
        FROM ChatMessage m
       WHERE m.chatRoom.id = :roomId
         AND m.createdAt < :cursorTime
       ORDER BY m.createdAt DESC
      """)
  List<ChatMessage> findPreviousMessages(
      @Param("roomId") UUID roomId,
      @Param("cursorTime") LocalDateTime cursorTime,
      Pageable pageable
  );
}
