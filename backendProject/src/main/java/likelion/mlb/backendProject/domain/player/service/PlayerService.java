package likelion.mlb.backendProject.domain.player.service;

import likelion.mlb.backendProject.domain.player.dto.PreviousBestPlayerDto;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.entity.live.PlayerFixtureStat;
import likelion.mlb.backendProject.domain.player.repository.PlayerFixtureStatRepository;
import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;
import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.domain.round.entity.Round;
import likelion.mlb.backendProject.domain.round.repository.FixtureRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.round.service.FixtureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerFixtureStatRepository playerFixtureStatRepository;
    private final FixtureService fixtureService;

    /**
     * 메인 페이지에 지난 라운드 베스트 플레이어들을 보여준다.
     * 시즌이 시작되지 않았다면 cost가 높은 선수들 출력
     * @return
     */
    public List<PreviousBestPlayerDto> getPreviousBestPlayer() {

            //이전 라운드의 경기들을 불러옴
            List<Fixture> fixtures = fixtureService.getFixturesOfPreviousRound();

            if(fixtures.size() == 0) {
                //아직 시즌이 시작되지 않았다면 (fixtures 값이 null이라면) 각 포지션별 cost가 높은 선수들을 리턴
                return fallbackBestPlayersByCost();
            }

            List<PlayerFixtureStat> dreamTeamStatsFetchPlayer = playerFixtureStatRepository.
                    findByFixtureInAndInDreamteamTrue(fixtures);
            return PreviousBestPlayerDto.toDto(dreamTeamStatsFetchPlayer);


    }

    private List<PreviousBestPlayerDto> fallbackBestPlayersByCost() {
        // FPL element_type id: 1=GK, 2=DEF, 3=MID, 4=FWD
        List<Player> players = new ArrayList<>();

        players.addAll(playerRepository.findBestByType(1, PageRequest.of(0, 3))); // GK 1
        players.addAll(playerRepository.findBestByType(2, PageRequest.of(0, 5))); // DEF 4
        players.addAll(playerRepository.findBestByType(3, PageRequest.of(0, 5))); // MID 3
        players.addAll(playerRepository.findBestByType(4, PageRequest.of(0, 3))); // FWD 3

        return PreviousBestPlayerDto.toDtoFromPlayer(players);
    }
}
