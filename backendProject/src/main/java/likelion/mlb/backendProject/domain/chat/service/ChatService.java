package likelion.mlb.backendProject.domain.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import org.springframework.stereotype.Service;

@Service
public interface ChatService {

  void sendMessage(ChatMessageDto dto);

  List<ChatMessageDto> getPreviousMessages(UUID roomId, LocalDateTime cursorTime, int size);
}
