package likelion.mlb.backendProject.domain.user.controller;

import likelion.mlb.backendProject.global.security.dto.CustomUserDetails;
import likelion.mlb.backendProject.domain.user.dto.UserMeResponse;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserMeController {

  @GetMapping("/me")
  public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal CustomUserDetails cud) {
    if (cud == null || cud.getUser() == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    var u = cud.getUser();
    return ResponseEntity.ok(new UserMeResponse(u.getId(), u.getEmail(), u.getName()));
  }
}