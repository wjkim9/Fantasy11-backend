package likelion.mlb.backendProject.domain.chat.repository;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  List<ChatMessage> findTop50ByChatRoomIdOrderByCreatedAtDesc(UUID chatRoomId);

  @Query("select m from ChatMessage m where m.chatRoomId = :roomId order by m.createdAt asc")
  List<ChatMessage> findAllByChatRoomIdOrderByCreatedAtAsc(UUID roomId);
}