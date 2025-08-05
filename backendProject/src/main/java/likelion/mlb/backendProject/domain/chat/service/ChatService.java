package likelion.mlb.backendProject.domain.chat.service;

import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import org.springframework.stereotype.Service;

@Service
public interface ChatService {

  void sendMessage(ChatMessageDto dto);

}
