package likelion.mlb.backendProject.domain.player.cache.service;

import likelion.mlb.backendProject.domain.player.cache.dto.ElementTypeDto;
import likelion.mlb.backendProject.domain.player.cache.dto.PlayerDto;
import likelion.mlb.backendProject.domain.player.cache.dto.TeamDto;
import likelion.mlb.backendProject.domain.player.elasticsearch.service.PlayerEsService;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerCacheService {

    private final PlayerRepository playerRepository;
    private final PlayerEsService playerEsService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PLAYER_CACHE_KEY = "players:all";
    private static final long CACHE_TTL_SECONDS = 3600; // 1시간

    /**
     * Player 엔티티 리스트 → PlayerDto 리스트 변환
     */
    private List<PlayerDto> toDtoList(List<Player> players) {
        return players.stream().map(p -> PlayerDto.builder()
                .id(p.getId())
                .code(p.getCode())
                .webName(p.getWebName())
                .krName(p.getKrName())
                .pic(p.getPic())
                .chanceOfPlayingNextRound(p.isChanceOfPlayingNextRound())
                .chanceOfPlayingThisRound(p.isChanceOfPlayingThisRound())
                .cost(p.getCost())
                .status(p.getStatus())
                .news(p.getNews())
                .teamCode(p.getTeamCode())
                .etId(p.getEtId())
                .team(TeamDto.builder()
                        .id(p.getTeam().getId())
                        .code(p.getTeam().getCode())
                        .fplId(p.getTeam().getFplId())
                        .name(p.getTeam().getName())
                        .krName(p.getTeam().getKrName())
                        .win(p.getTeam().getWin())
                        .draw(p.getTeam().getDraw())
                        .lose(p.getTeam().getLose())
                        .points(p.getTeam().getPoints())
                        .played(p.getTeam().getPlayed())
                        .position(p.getTeam().getPosition())
                        .build())
                .elementType(ElementTypeDto.builder()
                        .id(p.getElementType().getId())
                        .fplId(p.getElementType().getFplId())
                        .pluralName(p.getElementType().getPluralName())
                        .krName(p.getElementType().getKrName())
                        .squadMinPlay(p.getElementType().getSquadMinPlay())
                        .squadMaxPlay(p.getElementType().getSquadMaxPlay())
                        .elementCount(p.getElementType().getElementCount())
                        .build())
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * DB에서 선수 리스트를 조회하여 Redis에 DTO 형태로 저장 (TTL 적용)
     */
    public void loadPlayersToRedis() {
        List<Player> players = playerRepository.findAllWithTeamAndElementType();
        List<PlayerDto> playerDtoList = toDtoList(players);
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
        // 2. 캐시 미스 → DB 조회 + Redis 저장
        List<Player> players = playerRepository.findAllWithTeamAndElementType();
        List<PlayerDto> playerDtoList = toDtoList(players);
        redisTemplate.opsForValue().set(PLAYER_CACHE_KEY, playerDtoList, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return playerDtoList;
    }
}
