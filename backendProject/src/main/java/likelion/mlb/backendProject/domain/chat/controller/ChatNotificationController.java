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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Chat Notification", description = "채팅 알림 및 테스트 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify")
public class ChatNotificationController {


  private final SimpMessagingTemplate messagingTemplate;
  private final ChatRedisPublisher chatRedisPublisher;
  private final ChatNotificationService notificationService;
  private final ChatMessageService chatMessageService; // 디버그용

  @Operation(summary = "직접 알림 전송", description = "플레이어 이벤트 알림을 직접 전송합니다 (테스트용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "알림 전송 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
  })
  @PostMapping("/raw")
  public ResponseEntity<Void> raw(@Parameter(description = "알림 데이터") @RequestBody RawNotify req) {
    notificationService.sendMatchAlert(
        req.getPlayerId(), req.getFixtureId(), req.getEventType(),
        req.getMinute(), req.getPoint(), req.getText()
    );
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "채팅방 알림 전송", description = "특정 채팅방에 시스템 알림 메시지를 전송합니다 (테스트용)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "알림 전송 성공"),
      @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
  })
  @GetMapping("/room/{roomId}")
  public ResponseEntity<Map<String, Object>> roomAlert(
      @Parameter(description = "채팅방 ID", required = true) @PathVariable UUID roomId,
      @Parameter(description = "전송할 메시지 내용", required = true) @RequestParam String text) {

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
    chatRedisPublisher.publishToRoom(roomId, new java.util.HashMap<>(payload));

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