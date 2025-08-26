package likelion.mlb.backendProject.domain.player.elasticsearch.controller;

import likelion.mlb.backendProject.domain.player.elasticsearch.dto.PlayerEsDocument;
import likelion.mlb.backendProject.domain.player.elasticsearch.service.PlayerEsService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "Player Search", description = "선수 검색 API (Elasticsearch)")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/playerEs")
public class PlayerEsController {

    private final PlayerEsService playerEsService;

    @Operation(summary = "선수 검색", description = "키워드와 엘리먼트 타입으로 선수를 검색합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 검색 파라미터"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    @GetMapping("/search")
    public ResponseEntity<List<PlayerEsDocument>> searchWithKeyword(
            @Parameter(description = "검색 키워드 (선수명 등)") @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @Parameter(description = "엘리먼트 타입 ID") @RequestParam(value = "elementTypeId", defaultValue = "") String elementTypeId
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
