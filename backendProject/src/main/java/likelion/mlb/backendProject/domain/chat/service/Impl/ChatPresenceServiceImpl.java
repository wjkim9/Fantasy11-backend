package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import likelion.mlb.backendProject.domain.chat.dto.PresenceDto;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage.MessageType;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.event.ChatMessageEvent;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatPresenceService;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatPresenceServiceImpl implements ChatPresenceService {

  private final ChatRoomRepository roomRepo;
  private final ParticipantRepository participantRepo;
  private final ChatMessageRepository messageRepo;
  private final SimpMessagingTemplate messagingTemplate;
  private final RedisTemplate<String, String> redisTemplate;
  private final KafkaTemplate<String, ChatMessageEvent> kafkaTemplate;

  private String onlineKey(UUID roomId) {
    return "chat:room:" + roomId + ":online";
  }

  @Override
  @Transactional
  public ChatPresenceResponse join(UUID roomId, UUID userId) {
    ChatRoom room = roomRepo.findById(roomId)
        .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));

    boolean isParticipant = participantRepo.existsByDraft_IdAndUser_Id(room.getDraft().getId(), userId);
    if (!isParticipant) throw new BaseException(ErrorCode.ACCESS_DENIED);

    Long added = redisTemplate.opsForSet().add(onlineKey(roomId), userId.toString());
    long online = Objects.requireNonNullElse(redisTemplate.opsForSet().size(onlineKey(roomId)), 0L);

    if (added != null && added > 0) {
      ChatMessage sys = ChatMessage.builder()
          .id(UUID.randomUUID())
          .chatRoom(room)
          .user(null)
          .messageType(MessageType.SYSTEM)
          .content("사용자가 입장했습니다.")
          .createdAt(LocalDateTime.now())
          .build();
      messageRepo.save(sys);


      kafkaTemplate.send(
          "chat-messages",
          roomId.toString(),
          ChatMessageEvent.fromDto(
              sys.getId(), roomId, null, sys.getContent(),
              sys.getMessageType().name(), sys.getCreatedAt()
          )
      );

    }


    PresenceDto presence = new PresenceDto(roomId, userId, true, online);
    messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/presence", presence);

    return new ChatPresenceResponse(roomId, userId, true, online);
  }

  @Override
  @Transactional
  public ChatPresenceResponse leave(UUID roomId, UUID userId) {
    ChatRoom room = roomRepo.findById(roomId)
        .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));

    Long removed = redisTemplate.opsForSet().remove(onlineKey(roomId), userId.toString());
    long online = Objects.requireNonNullElse(redisTemplate.opsForSet().size(onlineKey(roomId)), 0L);

    if (removed != null && removed > 0) {
      ChatMessage sys = ChatMessage.builder()
          .id(UUID.randomUUID())
          .chatRoom(room)
          .user(null)
          .messageType(MessageType.SYSTEM)
          .content("사용자가 퇴장했습니다.")
          .createdAt(LocalDateTime.now())
          .build();
      messageRepo.save(sys);

      kafkaTemplate.send(
          "chat-messages",
          roomId.toString(),
          ChatMessageEvent.fromDto(
              sys.getId(), roomId, null, sys.getContent(),
              sys.getMessageType().name(), sys.getCreatedAt()
          )
      );
    }
    PresenceDto presence = new PresenceDto(roomId, userId, false, online);
    messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/presence", presence);

    return new ChatPresenceResponse(roomId, userId, false, online);
  }
}
