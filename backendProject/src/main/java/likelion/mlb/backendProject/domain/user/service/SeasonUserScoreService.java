package likelion.mlb.backendProject.domain.user.service;

import likelion.mlb.backendProject.domain.user.dto.SeasonUserScoreDto;
import likelion.mlb.backendProject.domain.user.entity.SeasonUserScore;
import likelion.mlb.backendProject.domain.user.repository.SeasonUserScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonUserScoreService {
    private final SeasonUserScoreRepository seasonUserScoreRepository;

    public List<SeasonUserScoreDto> getSeasonBestUsers() {
        List<SeasonUserScore> users = seasonUserScoreRepository.findBySeasonBestUsers("2526", PageRequest.of(0, 10));
        return SeasonUserScoreDto.toDto(users);

    }
}
