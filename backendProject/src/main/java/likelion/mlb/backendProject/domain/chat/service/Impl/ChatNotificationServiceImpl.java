package likelion.mlb.backendProject.domain.chat.service.Impl;


import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import likelion.mlb.backendProject.domain.chat.bus.ChatRedisPublisher;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.repository.AlertRoutingRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatMessageService;
import likelion.mlb.backendProject.domain.chat.service.ChatNotificationService;
import likelion.mlb.backendProject.domain.player.entity.live.MatchEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ChatNotificationServiceImpl implements ChatNotificationService {

  private final AlertRoutingRepository routingRepo;
  private final ChatMessageRepository chatMessageRepository;
  private final ChatMessageService chatMessageService;
  private final SimpMessagingTemplate messagingTemplate;
  private final ChatRedisPublisher chatRedisPublisher;

  /**
   * LiveDataService에서 바로 호출 (현재 트랜잭션 커밋 후에 실행되도록 지연)
   */
  @Override
  public void sendMatchAlert(MatchEvent event) {
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          doDispatch(event.getPlayer().getId(), event.getFixture().getId(),
              event.getEventType(), event.getMinute(), (int) event.getPoint(), null);
        }
      });
    } else {
      doDispatch(event.getPlayer().getId(), event.getFixture().getId(),
          event.getEventType(), event.getMinute(), (int) event.getPoint(), null);
    }
  }

  @Override
  public void sendMatchAlert(UUID playerId, UUID fixtureId, String eventType, Integer minute, Integer point,
      String text) {
    doDispatch(playerId, fixtureId, eventType, minute, point, text);
  }

  /**
   * 방에 저장+브로드캐스트 (새 트랜잭션)
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  protected void doDispatch(UUID playerId,
      UUID fixtureId,
      String eventType,
      Integer minute,
      Integer point,
      String fallbackText) {

    List<UUID> roomIds = routingRepo.findChatRoomIdsForEvent(playerId, fixtureId);
    if (roomIds.isEmpty()) return;

    String msgText = (fallbackText != null && !fallbackText.isBlank())
        ? fallbackText
        : formatText(eventType, minute, point);

    for (UUID roomId : roomIds) {
      // ✅ 서비스 경유 저장: AFTER_COMMIT 색인(ES)까지 자동 수행
      var saved = chatMessageService.saveSystemAlert(roomId, msgText);

      var payload = Map.of(
          "id", saved.getId().toString(),
          "chatRoomId", roomId.toString(),
          "type", "ALERT",
          "content", saved.getContent(),
          "createdAt", saved.getCreatedAt().toString()
      );
      messagingTemplate.convertAndSend("/topic/chat/" + roomId, payload);
      chatRedisPublisher.publishToRoom(roomId, new java.util.HashMap<>(payload));
    }
  }

  private String formatText(String eventType, Integer minute, Integer point) {
    String norm = (eventType == null) ? "" : eventType.toLowerCase();
    String emoji = switch (norm) {
      case "goals_scored", "goal", "골" -> "⚽";
      case "assist", "어시스트" -> "🅰️";
      default -> "🔔";
    };
    String min = (minute != null && minute > 0) ? minute + "’ " : "";
    String pts = (point != null && point != 0) ? " +" + point : "";
    String label = switch (norm) {
      case "goals_scored", "goal", "골" -> "골";
      case "assist", "어시스트" -> "어시스트";
      default -> "이벤트";
    };
    return emoji + " " + min + label + pts;
  }
}
