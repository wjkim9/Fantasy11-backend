package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.event.ChatMessageEvent;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;

import likelion.mlb.backendProject.domain.chat.service.ChatNotificationService;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.domain.player.entity.live.MatchEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatNotificationServiceImpl implements ChatNotificationService {

  private final DraftRepository draftRepo;
  private final ChatRoomRepository chatRoomRepo;
  private final ChatMessageRepository chatMessageRepo;
  private final KafkaTemplate<String, ChatMessageEvent> kafkaTemplate;

  @Override
  @Transactional
  public void sendMatchAlert(MatchEvent matchEvent) {
    UUID roundId = matchEvent.getFixture().getRound().getId();

    UUID draftId = draftRepo.findByRoundId(roundId)
        .orElseThrow(() -> new IllegalStateException("Draft not found for round " + roundId))
        .getId();

    ChatRoom room = chatRoomRepo.findByDraftId(draftId)
        .orElseThrow(() -> new IllegalStateException("ChatRoom not found for draft " + draftId));

    String playerName = matchEvent.getPlayer().getKrName();
    String content;
    switch (matchEvent.getEventType()) {
      case "GOAL":
        content = String.format("%s님이 %d분에 골을 넣었습니다!", playerName, matchEvent.getMinute()); break;
      case "ASSIST":
        content = String.format("%s님이 %d분에 어시스트를 기록했습니다!", playerName, matchEvent.getMinute()); break;
      default:
        content = String.format("%s님이 %d분에 %s 이벤트가 발생했습니다.",
            playerName, matchEvent.getMinute(), matchEvent.getEventType());
    }

    ChatMessage alert = ChatMessage.builder()
        .id(UUID.randomUUID())
        .chatRoom(room)
        .messageType(ChatMessage.MessageType.ALERT)
        .content(content)
        .createdAt(LocalDateTime.now())
        .user(null)
        .build();
    chatMessageRepo.save(alert);


    kafkaTemplate.send(
        "chat-messages",
        room.getId().toString(),
        ChatMessageEvent.fromDto(
            alert.getId(), room.getId(), null,
            alert.getContent(), alert.getMessageType().name(), alert.getCreatedAt()
        )
    );
  }
}