package likelion.mlb.backendProject.domain.chat.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ChatMessageQueryRepository extends JpaRepository<ChatMessage, UUID> {

  /** 최신부터 N개 (createdAt desc, id desc) */
  @Query("""
      select m from ChatMessage m
      where m.chatRoomId = :roomId
      order by m.createdAt desc, m.id desc
      """)
  List<ChatMessage> findRecent(
      @Param("roomId") UUID roomId,
      Pageable pageable
  );

  /** 커서보다 옛 메시지 N개 (키셋) */
  @Query("""
      select m from ChatMessage m
      where m.chatRoomId = :roomId
        and (m.createdAt < :cursorAt
          or (m.createdAt = :cursorAt and m.id < :cursorId))
      order by m.createdAt desc, m.id desc
      """)
  List<ChatMessage> findBefore(
      @Param("roomId") UUID roomId,
      @Param("cursorAt") Instant cursorAt,
      @Param("cursorId") UUID cursorId,
      Pageable pageable
  );
}