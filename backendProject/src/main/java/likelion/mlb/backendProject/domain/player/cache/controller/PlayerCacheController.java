package likelion.mlb.backendProject.domain.player.cache.controller;

import likelion.mlb.backendProject.domain.player.cache.dto.PlayerDto;
import likelion.mlb.backendProject.domain.player.cache.service.PlayerCacheService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/playerCache")
@RequiredArgsConstructor
public class PlayerCacheController {

    private final PlayerCacheService playerCacheService;

    // DB → Redis 저장 (캐시 초기화)
    @PostMapping("/cache")
    public ResponseEntity<String> cachePlayerList() {
        playerCacheService.loadPlayersToRedis();
        return ResponseEntity.ok("Player list cached to Redis.");
    }

    // Redis → 조회 (캐시 미스 시 자동 로드)
    @GetMapping
    public ResponseEntity<List<PlayerDto>> getPlayerList() {
        List<PlayerDto> playerList = playerCacheService.getPlayersFromRedis();
        return ResponseEntity.ok(playerList);
    }
}