package likelion.mlb.backendProject.domain.player.cache.controller;

import likelion.mlb.backendProject.domain.player.cache.dto.PlayerDto;
import likelion.mlb.backendProject.domain.player.cache.service.PlayerCacheService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.*;

@Tag(name = "Player Cache", description = "선수 캐시 관리 API")
@RestController
@RequestMapping("/api/playerCache")
@RequiredArgsConstructor
public class PlayerCacheController {

    private final PlayerCacheService playerCacheService;

    @Operation(summary = "선수 데이터 캐시링", description = "DB의 선수 데이터를 Redis 캐시로 로드합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "캐시링 성공"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    @PostMapping("/cache")
    public ResponseEntity<String> cachePlayerList() {
        playerCacheService.loadPlayersToRedis();
        return ResponseEntity.ok("Player list cached to Redis.");
    }

    @Operation(summary = "선수 목록 조회", description = "Redis 캐시에서 선수 목록을 조회합니다 (캐시 미스 시 DB에서 자동 로드)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "선수 목록 조회 성공"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    @GetMapping
    public ResponseEntity<List<PlayerDto>> getPlayerList() {
        List<PlayerDto> playerList = playerCacheService.getPlayersFromRedis();
        return ResponseEntity.ok(playerList);
    }
}