package likelion.mlb.backendProject.domain.match.controller;

import likelion.mlb.backendProject.domain.match.dto.MatchStatusResponse;
import likelion.mlb.backendProject.domain.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchRestController {

    private final MatchService matchService;

    @GetMapping("/status")
    public ResponseEntity<MatchStatusResponse> getStatus() {
        return ResponseEntity.ok(matchService.getCurrentStatus());
    }

}
