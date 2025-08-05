package likelion.mlb.backendProject.domain.chat.repository;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,UUID> {

}
