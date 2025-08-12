package likelion.mlb.backendProject.domain.chat.repository;

import java.util.Optional;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

  Optional<ChatRoom> findByDraft(Draft draft);
  Optional<ChatRoom> findByDraftId(UUID draftId);

}
