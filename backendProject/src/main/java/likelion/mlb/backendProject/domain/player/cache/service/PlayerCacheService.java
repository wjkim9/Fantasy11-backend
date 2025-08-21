package likelion.mlb.backendProject.domain.player.cache.service;

import likelion.mlb.backendProject.domain.player.cache.dto.PlayerDto;
import likelion.mlb.backendProject.domain.player.elasticsearch.service.PlayerEsService;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PlayerCacheService {

    private final PlayerRepository playerRepository;
    private final PlayerEsService playerEsService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PLAYER_CACHE_KEY = "players:all";
    private static final long CACHE_TTL_SECONDS = 7200; // 2시간

    /**
     * DB에서 선수 리스트를 조회하여 Redis에 DTO 형태로 저장 (TTL 적용)
     */
    public void loadPlayersToRedis() {
        List<Player> players = playerRepository.findAllWithTeamAndElementType();
        List<PlayerDto> playerDtoList = Player.toDtoList(players);
        redisTemplate.opsForValue().set(PLAYER_CACHE_KEY, playerDtoList, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

        // Elasticsearch에도 저장
        playerEsService.saveAll(PlayerDto.toDocumentList(playerDtoList));
    }

    /**
     * Redis에서 DTO 리스트 조회 (캐시 미스 시 DB 조회 후 저장)
     */
    public List<PlayerDto> getPlayersFromRedis() {
        Object cached = redisTemplate.opsForValue().get(PLAYER_CACHE_KEY);

        // 1. redis에 저장 된 데이터 가져오기
        if (cached instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<PlayerDto> playerDtoCacheList = (List<PlayerDto>) cached;
            return playerDtoCacheList;
        }
        // 2. 캐시 미스 → DB 조회 + Redis 저장 + Elasticsearch에도 저장
        List<Player> playerList = playerRepository.findAllWithTeamAndElementType();
        List<PlayerDto> playerDtoList = Player.toDtoList(playerList);
        redisTemplate.opsForValue().set(PLAYER_CACHE_KEY, playerDtoList, CACHE_TTL_SECONDS, TimeUnit.SECONDS);

        // Elasticsearch에도 저장
        // (기존에 elsticsearch에 데이터가 있을 시 PlayerEsDocument의 @Id비교해서 같으면 update, 다르면 insert)
        playerEsService.saveAll(PlayerDto.toDocumentList(playerDtoList));

        return playerDtoList;
    }
}
