package likelion.mlb.backendProject.domain.match.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WellKnownIgnoreController {

    @GetMapping("/.well-known/**")
    public ResponseEntity<Void> ignore() {
        return ResponseEntity.notFound().build(); // 404 보내주기
    }
}

