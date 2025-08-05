package likelion.mlb.backendProject.domain.chat.service.Impl;


import java.time.LocalDateTime;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatService;
import likelion.mlb.backendProject.domain.user.entity.User;
import likelion.mlb.backendProject.domain.user.repository.UserRepository;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final UserRepository userRepo;
  private final ChatRoomRepository roomRepo;
  private final ChatMessageRepository messageRepo;
  private final SimpMessagingTemplate template;

  public void sendMessage(ChatMessageDto dto){
    //어떤 방에다가 넣어줄지
    ChatRoom room = roomRepo.findById(dto.getRoomId())
        .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));

    ChatMessage msg = new ChatMessage();
    msg.setId(UUID.randomUUID());
    msg.setChatRoom(room);
    msg.setContent(dto.getContent());
    msg.setMessageType(dto.getMessageType());
    msg.setCreatedAt(LocalDateTime.now());
    //유저
    if (dto.getUserId() != null) {
      User user = userRepo.findById(dto.getUserId())
          .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
    }
    messageRepo.save(msg);

    dto.setId(msg.getId());
    dto.setSentAt(msg.getCreatedAt());

    String dest = "/topic/chat/" + dto.getRoomId();
    template.convertAndSend(dest, dto);
  }
}
