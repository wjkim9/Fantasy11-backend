package likelion.mlb.backendProject.domain.chat.repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatReadState;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatReadStateRepository extends JpaRepository<ChatReadState, UUID> {

  Optional<ChatReadState> findByChatRoomIdAndUserId(UUID chatRoomId, UUID userId);

  @Query("""
      select count(m)
        from ChatMessage m
       where m.chatRoomId = :roomId
         and m.createdAt > :since
    """)
  long countUnread(@Param("roomId") UUID roomId, @Param("since") Instant since);
}
