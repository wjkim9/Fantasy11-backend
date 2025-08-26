package likelion.mlb.backendProject.domain.chat.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
public class ChatMessagingController {

  private final ChatMessageService chatMessageService;
  private final SimpMessagingTemplate messagingTemplate;
  private final ChatMembershipRepository membershipRepository;
  private final ChatRedisPublisher chatRedisPublisher;


  @Transactional
  @MessageMapping("/chat/send")
  public void send(ChatSendRequest req,
      Principal principal) throws JsonProcessingException {

    UUID userId = null;
    if (principal instanceof Authentication auth
        && auth.getPrincipal() instanceof CustomUserDetails cud) {
      userId = cud.getUser().getId(); // UUID
    }

    // ✅ 방 멤버인지 권한 체크 (아니면 바로 거절)
    if (userId == null || !membershipRepository.isMember(req.getRoomId(), userId)) {
      throw new org.springframework.messaging.MessagingException("Not a member of this chat room");
      // 또는 그냥 return;  // 조용히 무시하고 싶으면
    }


    System.out.println("------------/chat/{roomId}/send 시작 ");

    // 안전장치: 메시지의 roomId는 URL의 roomId로 강제
    ChatMessage saved = chatMessageService.saveUserMessage(req.getRoomId(), userId, req.getContent());

    Map<String, Object> payload = Map.of(
        "id", saved.getId().toString(),
        "chatRoomId", req.getRoomId().toString(),
        "type", saved.getMessageType().name(),
        "content", saved.getContent(),
        "userId", userId != null ? userId.toString() : null,
        "createdAt", saved.getCreatedAt().toString()
    );


    //System.out.println("------------받은메세지 payload"+payload.get(0).toString());

    System.out.println("------------받은메세지"+req.getContent());

    // ✅ 즉시 현재 노드의 클라이언트에게 전달
    messagingTemplate.convertAndSend("/topic/chat/" + req.getRoomId(), payload);
    
    // ✅ 다른 노드를 위해 Redis로도 전달
    chatRedisPublisher.publishToRoom("chat." + req.getRoomId(), payload);
  }
}
