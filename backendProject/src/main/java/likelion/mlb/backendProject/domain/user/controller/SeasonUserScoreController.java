package likelion.mlb.backendProject.domain.user.controller;

import likelion.mlb.backendProject.domain.user.dto.SeasonUserScoreDto;
import likelion.mlb.backendProject.domain.user.service.SeasonUserScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class SeasonUserScoreController {

    private final SeasonUserScoreService seasonUserScoreService;

    @GetMapping("/seasonBestScore")
    public ResponseEntity<List<SeasonUserScoreDto>> getSeasonBestScore() {
        List<SeasonUserScoreDto> dtos = seasonUserScoreService.getSeasonBestUsers();
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
}
