package likelion.mlb.backendProject.domain.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import likelion.mlb.backendProject.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @MessageMapping("/chat.send")
  public void sendMessage(@Payload ChatMessageDto dto, java.security.Principal principal) {

    var auth = (org.springframework.security.core.Authentication) principal;
    var cud  = (likelion.mlb.backendProject.global.security.dto.CustomUserDetails) auth.getPrincipal();

    dto.setUserId(cud.getUser().getId());

    chatService.sendMessage(dto);
  }
}
