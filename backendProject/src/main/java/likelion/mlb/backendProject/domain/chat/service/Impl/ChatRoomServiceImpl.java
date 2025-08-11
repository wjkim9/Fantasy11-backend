package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.time.LocalDateTime;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import likelion.mlb.backendProject.domain.chat.dto.ChatRoomDto;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage.MessageType;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.event.ChatMessageEvent;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomService;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
  private final ChatRoomRepository roomRepo;
  private final DraftRepository draftRepo;
  private final ChatMessageRepository chatMessageRepo;
  private final KafkaTemplate<String, ChatMessageEvent> kafkaTemplate;

  @Override
  public ChatRoomDto createRoom(UUID draftId) {

    Draft draft = draftRepo.findById(draftId)
        .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));


    roomRepo.findByDraft(draft).ifPresent(room -> {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    });


    ChatRoom room = new ChatRoom();
    room.setId(UUID.randomUUID());
    room.setDraft(draft);
    room.setCreatedAt(LocalDateTime.now());
    roomRepo.save(room);

    ChatMessage sysMsg = ChatMessage.builder()
        .id(UUID.randomUUID())
        .chatRoom(room)
        .messageType(MessageType.SYSTEM)
        .content("채팅방이 생성되었습니다.")
        .createdAt(LocalDateTime.now())
        .user(null)
        .build();
    chatMessageRepo.save(sysMsg);

    kafkaTemplate.send(
        "chat-messages",
        room.getId().toString(),
        ChatMessageEvent.fromDto(
            sysMsg.getId(),
            room.getId(),
            null,
            sysMsg.getContent(),
            sysMsg.getMessageType().name(),
            sysMsg.getCreatedAt()
        )
    );



    return new ChatRoomDto(room.getId(), draft.getId(), room.getCreatedAt());
  }

}
