package likelion.mlb.backendProject.global.security.controller;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session-tokens")
public class SessionTokenController {

  @GetMapping
  public Map<String, String> getTokens(HttpSession session) {
    String accessToken = (String) session.getAttribute("accessToken");
    String refreshToken = (String) session.getAttribute("refreshToken");

    return Map.of(
        "accessToken", accessToken != null ? accessToken : "",
        "refreshToken", refreshToken != null ? refreshToken : ""
    );
  }
}
