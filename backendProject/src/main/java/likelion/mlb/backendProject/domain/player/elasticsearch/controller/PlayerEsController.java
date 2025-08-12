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


    // elasticsearch 검색 결과를 json List로 반환
    @GetMapping("/elasticsearch")
    public ResponseEntity<List<PlayerEsDocument>> elasticSearch(
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {

        return ResponseEntity.ok(playerEsService.search(keyword));
    }
}
