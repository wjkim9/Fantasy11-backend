package likelion.mlb.backendProject.domain.chat.repository;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
  Optional<ChatRoom> findByDraftId(UUID draftId);
}