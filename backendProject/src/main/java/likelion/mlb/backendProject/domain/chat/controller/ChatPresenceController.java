package likelion.mlb.backendProject.domain.chat.controller;

import likelion.mlb.backendProject.domain.chat.dto.PresenceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatPresenceController {

  private final SimpMessagingTemplate template;

  /**
   * 입장할 때 호출: 클라이언트가 /app/chat.join 으로 보내면 실행
   */
  @MessageMapping("/chat.join")
  public void join(@Payload PresenceDto dto) {
    dto.setOnline(true);
    template.convertAndSend(
        "/topic/chat/" + dto.getRoomId() + "/presence",
        dto
    );
  }

  /**
   * 나갈 때 호출: 클라이언트가 /app/chat.leave 으로 보내면 실행
   */
  @MessageMapping("/chat.leave")
  public void leave(@Payload PresenceDto dto) {
    dto.setOnline(false);
    template.convertAndSend(
        "/topic/chat/" + dto.getRoomId() + "/presence",
        dto
    );
  }
}