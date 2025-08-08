package likelion.mlb.backendProject.domain.chat.controller;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatRoomDto;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;


  @PostMapping
  public ResponseEntity<ChatRoomDto> createRoom(@RequestParam UUID draftId) {
    ChatRoomDto dto = chatRoomService.createRoom(draftId);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(dto);
  }
}