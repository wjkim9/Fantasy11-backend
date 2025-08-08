package likelion.mlb.backendProject.domain.player.service;

import likelion.mlb.backendProject.domain.player.dto.PreviousBestPlayerDto;
import likelion.mlb.backendProject.domain.player.entity.live.PlayerFixtureStat;
import likelion.mlb.backendProject.domain.player.repository.PlayerFixtureStatRepository;
import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;
import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.domain.round.entity.Round;
import likelion.mlb.backendProject.domain.round.repository.FixtureRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.round.service.FixtureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerFixtureStatRepository playerFixtureStatRepository;
    private final FixtureService fixtureService;

    public List<PreviousBestPlayerDto> getPreviousBestPlayer() {
        //이전 라운드의 경기들을 불러옴
        List<Fixture> fixturesOfPreviousRound = fixtureService.getFixturesOfPreviousRound();

        //경기들에서 inDreamteam이 true인 선수들 정보를 가져옴
        List<PlayerFixtureStat> dreamTeamStatsFetchPlayer = playerFixtureStatRepository.
                findByFixtureInAndInDreamteamTrue(fixturesOfPreviousRound);
        List<PreviousBestPlayerDto> dto = PreviousBestPlayerDto.toDto(dreamTeamStatsFetchPlayer);
        return dto;
    }
}
