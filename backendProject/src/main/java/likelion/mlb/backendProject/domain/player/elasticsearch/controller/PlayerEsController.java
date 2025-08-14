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
    @GetMapping("/search")
    public ResponseEntity<List<PlayerEsDocument>> searchWithKeyword(
            @RequestParam(value = "keyword", defaultValue = "") String keyword
            , @RequestParam(value = "elementTypeId", defaultValue = "") String elementTypeId
            ) {

        return ResponseEntity.ok(playerEsService.search(keyword, elementTypeId));
    }

    /*
    @GetMapping("/searchWithKeyword")
    public ResponseEntity<List<PlayerEsDocument>> searchWithKeyword(
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {

        return ResponseEntity.ok(playerEsService.searchWithKeyword(keyword));
    }

    // elasticsearch 검색 결과를 json List로 반환
    @GetMapping("/searchWithElementTypeId")
    public ResponseEntity<List<PlayerEsDocument>> searchWithElementTypeId(
            @RequestParam(value = "elementTypeId", defaultValue = "") String elementTypeId) {

        return ResponseEntity.ok(playerEsService.searchWithElementTypeId(elementTypeId));
    }
     */
}
