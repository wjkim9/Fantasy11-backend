package likelion.mlb.backendProject.domain.round.service;

import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.domain.round.repository.FixtureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FixtureService {

    private final FixtureRepository fixtureRepository;

    public List<Fixture> getFixturesOfPreviousRound() {
        return fixtureRepository.findByRoundIsPreviousTrue();
    }
}
