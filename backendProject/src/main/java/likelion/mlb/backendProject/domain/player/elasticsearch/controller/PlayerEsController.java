package likelion.mlb.backendProject.domain.player.elasticsearch.controller;

import likelion.mlb.backendProject.domain.player.elasticsearch.dto.PlayerEsDocument;
import likelion.mlb.backendProject.domain.player.elasticsearch.service.PlayerEsService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/playerEs")
public class PlayerEsController {

    private final PlayerEsService playerEsService;

//    private final KafkaTemplate<String, SearchLogMessage> kafkaTemplate;


    // elasticsearch 검색 결과를 json List로 반환
    @GetMapping("/elasticsearch")
    public ResponseEntity<List<PlayerEsDocument>> elasticSearch(
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {

        // 검색어 정보 카프카 전송
        String userId = "1";
        String searchedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

//        SearchLogMessage message = new SearchLogMessage(keyword, userId, searchedAt);
//        kafkaTemplate.send("search-log", message); // search-log 토픽으로 메시지 전달

        return ResponseEntity.ok(playerEsService.search(keyword));
    }

    // 검색어 top 10개 가져오기
    /*
    @GetMapping("/top-keywords")
    public ResponseEntity<List<String>> getTopKeyWord() {
        List<String> keywords = boardEsService.getTopSearchKeyword();
        return ResponseEntity.ok(keywords);
    }
    */
}
