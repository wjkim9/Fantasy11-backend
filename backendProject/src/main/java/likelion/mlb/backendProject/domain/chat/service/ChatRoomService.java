package likelion.mlb.backendProject.domain.chat.service;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatRoomDto;
import org.springframework.stereotype.Service;

@Service
public interface ChatRoomService {

  ChatRoomDto createRoom(UUID draftId);
}
