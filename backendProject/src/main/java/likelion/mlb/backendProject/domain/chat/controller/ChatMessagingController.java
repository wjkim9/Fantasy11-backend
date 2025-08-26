package likelion.mlb.backendProject.domain.chat.controller;


import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.bus.ChatRedisPublisher;
import likelion.mlb.backendProject.domain.chat.dto.ChatSendRequest;
import likelion.mlb.backendProject.domain.chat.repository.ChatMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.service.ChatMessageService;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;

@Controller
@RequiredArgsConstructor
public class ChatMessagingController {

  private final ChatMessageService chatMessageService;
  private final SimpMessagingTemplate messagingTemplate;
  private final ChatMembershipRepository membershipRepository;
  private final ChatRedisPublisher chatRedisPublisher;


  @MessageMapping("/chat/{roomId}/send")
  public void send(@DestinationVariable UUID roomId,
      ChatSendRequest req,
      Principal principal) {

    UUID userId = null;
    if (principal instanceof Authentication auth
        && auth.getPrincipal() instanceof CustomUserDetails cud) {
      userId = cud.getUser().getId(); // UUID
    }

    // ✅ 방 멤버인지 권한 체크 (아니면 바로 거절)
    if (userId == null || !membershipRepository.isMember(roomId, userId)) {
      throw new org.springframework.messaging.MessagingException("Not a member of this chat room");
      // 또는 그냥 return;  // 조용히 무시하고 싶으면
    }

    // 안전장치: 메시지의 roomId는 URL의 roomId로 강제
    ChatMessage saved = chatMessageService.saveUserMessage(roomId, userId, req.getContent());

    Map<String, Object> payload = Map.of(
        "id", saved.getId().toString(),
        "chatRoomId", roomId.toString(),
        "type", saved.getMessageType().name(),
        "content", saved.getContent(),
        "userId", userId != null ? userId.toString() : null,
        "createdAt", saved.getCreatedAt().toString()
    );

    // Redis pub/sub 방식으로 전송
    chatRedisPublisher.publishToRoom(roomId, new java.util.HashMap<>(payload));
  }
}
