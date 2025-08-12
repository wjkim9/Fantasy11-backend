package likelion.mlb.backendProject.domain.team.service;


import likelion.mlb.backendProject.domain.team.dto.TeamTableDto;
import likelion.mlb.backendProject.domain.team.entity.Team;
import likelion.mlb.backendProject.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;


    public List<TeamTableDto> getTeamTable() {
        return TeamTableDto.toDto(teamRepository.findAllByOrderByPositionAsc());
    }
}
