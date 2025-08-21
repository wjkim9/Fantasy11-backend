package likelion.mlb.backendProject.domain.user.controller;

import java.util.Map;
import java.util.UUID;

import likelion.mlb.backendProject.domain.user.service.UserRoomQueryService;
import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class CurrentRoomController {

  private final UserRoomQueryService userRoomQueryService;

  @GetMapping("/current-room")
  public ResponseEntity<?> getCurrentRoom(@AuthenticationPrincipal CustomUserDetails cud) {
    if (cud == null || cud.getUser() == null) {
      return ResponseEntity.status(401).build();
    }
    UUID userId = cud.getUser().getId();

    return userRoomQueryService.findLatestChatRoomForUser(userId)
        .<ResponseEntity<?>>map(roomId -> ResponseEntity.ok(Map.of("roomId", roomId.toString())))
        .orElseGet(() -> ResponseEntity.ok(Map.of())); // { } → 프론트의 “없음” 처리 로직과 호환
  }
}
