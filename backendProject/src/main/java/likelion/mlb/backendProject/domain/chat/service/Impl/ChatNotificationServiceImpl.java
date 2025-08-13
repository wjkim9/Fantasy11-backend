package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.event.ChatMessageEvent;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatNotificationService;
import likelion.mlb.backendProject.domain.draft.repository.ParticipantPlayerRepository;
import likelion.mlb.backendProject.domain.player.entity.live.MatchEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatNotificationServiceImpl implements ChatNotificationService {

  private final ParticipantPlayerRepository participantPlayerRepo;
  private final ChatRoomRepository chatRoomRepo;
  private final ChatMessageRepository chatMessageRepo;
  private final KafkaTemplate<String, ChatMessageEvent> kafkaTemplate;

  @Override
  @Transactional
  public void sendMatchAlert(MatchEvent matchEvent) {

    UUID roundId  = matchEvent.getFixture().getRound().getId();
    UUID playerId = matchEvent.getPlayer().getId();

    // 1) 이 선수를 소유한 "해당 라운드의" 모든 드래프트 찾기
    List<UUID> draftIds = participantPlayerRepo.findDraftIdsByPlayerAndRound(playerId, roundId);
    if (draftIds == null || draftIds.isEmpty()) {
      // 아무도 그 선수를 뽑지 않은 경우 → 전송할 방 없음
      return;
    }

    // 2) 메시지 내용 생성 (팀원 규칙에 맞춘 eventType: goals_scored / assist)
    String playerName = matchEvent.getPlayer().getKrName();
    String content;
    switch (matchEvent.getEventType()) {
      case "goals_scored":
        content = String.format("%s님이 %d분에 골을 넣었습니다!", playerName, matchEvent.getMinute());
        break;
      case "assist":
        content = String.format("%s님이 %d분에 어시스트를 기록했습니다!", playerName, matchEvent.getMinute());
        break;
      default:
        content = String.format("%s님이 %d분에 %s 이벤트가 발생했습니다.",
            playerName, matchEvent.getMinute(), matchEvent.getEventType());
    }

    // 3) 각 드래프트의 채팅방으로 ALERT 팬아웃 전송
    for (UUID draftId : draftIds) {
      chatRoomRepo.findByDraftId(draftId).ifPresent(room -> {
        // DB 저장
        ChatMessage alert = ChatMessage.builder()
            .id(UUID.randomUUID())
            .chatRoom(room)
            .messageType(ChatMessage.MessageType.ALERT)
            .content(content)
            .createdAt(LocalDateTime.now())
            .user(null)
            .build();
        chatMessageRepo.save(alert);

        // Kafka → ES 색인 & 프론트 브로드캐스트(인덱서가 처리)
        kafkaTemplate.send(
            "chat-messages",
            room.getId().toString(),
            ChatMessageEvent.fromDto(
                alert.getId(), room.getId(), null,
                alert.getContent(), alert.getMessageType().name(), alert.getCreatedAt()
            )
        );
      });
    }
  }
}
