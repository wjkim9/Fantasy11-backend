package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.event.ChatMessageEvent;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatService;
import likelion.mlb.backendProject.domain.user.entity.User;
import likelion.mlb.backendProject.domain.user.repository.UserRepository;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final UserRepository userRepo;
  private final ChatRoomRepository roomRepo;
  private final ChatMessageRepository messageRepo;
  private final SimpMessagingTemplate template;
  private final KafkaTemplate<String, ChatMessageEvent> kafkaTemplate;



  public void sendMessage(ChatMessageDto dto) {
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

    ChatMessageEvent evt = ChatMessageEvent.fromDto(
        msg.getId(),
        room.getId(),
        dto.getUserId(),
        dto.getContent(),
        dto.getMessageType().name(),
        msg.getCreatedAt()
    );

    kafkaTemplate.send(
        "chat-messages",
        evt.getRoomId().toString(),
        evt
    );

    template.convertAndSend("/topic/chat/" + room.getId(), dto);

    dto.setId(msg.getId());
    dto.setSentAt(msg.getCreatedAt());

    String dest = "/topic/chat/" + dto.getRoomId();
    template.convertAndSend(dest, dto);
  }

  @Override
  public List<ChatMessageDto> getPreviousMessages(UUID roomId, LocalDateTime cursorTime, int size) {

    roomRepo.findById(roomId)
        .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));


    List<ChatMessage> page = messageRepo.findPreviousMessages(
        roomId,
        cursorTime,
        PageRequest.of(0, size)
    );


    Collections.reverse(page);

    return page.stream()
        .map(msg -> new ChatMessageDto(
            msg.getId(),
            msg.getChatRoom().getId(),
            msg.getUser() != null ? msg.getUser().getId() : null,
            msg.getMessageType(),
            msg.getContent(),
            msg.getCreatedAt()
        ))
        .collect(Collectors.toList());
  }
}
