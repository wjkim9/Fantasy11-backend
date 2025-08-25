package likelion.mlb.backendProject.domain.chat.controller;

/**
 * 개발 테스트용 컨트롤러!!
 * */
import java.util.Map;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.bus.ChatRedisPublisher;
import likelion.mlb.backendProject.domain.chat.service.ChatMessageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import likelion.mlb.backendProject.domain.chat.service.ChatNotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify")
public class ChatNotificationController {


  private final SimpMessagingTemplate messagingTemplate;
  private final ChatRedisPublisher chatRedisPublisher;
  private final ChatNotificationService notificationService;
  private final ChatMessageService chatMessageService; // 디버그용

  @PostMapping("/raw")
  public ResponseEntity<Void> raw(@RequestBody RawNotify req) {
    notificationService.sendMatchAlert(
        req.getPlayerId(), req.getFixtureId(), req.getEventType(),
        req.getMinute(), req.getPoint(), req.getText()
    );
    return ResponseEntity.ok().build();
  }

  @GetMapping("/room/{roomId}")
  public ResponseEntity<Map<String, Object>> roomAlert(
      @PathVariable UUID roomId,
      @RequestParam String text) {

    var saved = chatMessageService.saveSystemAlert(roomId, text); // DB 저장

    // ✅ STOMP 브로드캐스트 + Redis fan-out
    Map<String, Object> payload = Map.of(
        "id", saved.getId().toString(),
        "chatRoomId", roomId.toString(),
        "type", "ALERT",
        "content", saved.getContent(),
        "createdAt", saved.getCreatedAt().toString()
    );
    messagingTemplate.convertAndSend("/topic/chat/" + roomId, payload);
   // chatRedisPublisher.publishToRoom(roomId, new java.util.HashMap<>(payload));

    return ResponseEntity.ok(Map.of("ok", true, "id", saved.getId().toString()));
  }


  @Data
  public static class RawNotify {

    private UUID playerId;
    private UUID fixtureId;
    private String eventType; // "goals_scored", "assist" ...
    private Integer minute;   // nullable ok
    private Integer point;    // nullable ok
    private String text;      // 선택 메시지
  }
}