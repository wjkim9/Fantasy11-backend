package likelion.mlb.backendProject.domain.player.controller;

import likelion.mlb.backendProject.domain.player.dto.PreviousBestPlayerDto;
import likelion.mlb.backendProject.domain.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/previousPlayer")
    public ResponseEntity<List<PreviousBestPlayerDto>> getPreviousBestPlayer() {
        List<PreviousBestPlayerDto> previousBestPlayers = playerService.getPreviousBestPlayer();
        return new ResponseEntity<>(previousBestPlayers, HttpStatus.OK);
    }
}
