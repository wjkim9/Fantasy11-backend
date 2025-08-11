package likelion.mlb.backendProject.domain.team.controller;


import likelion.mlb.backendProject.domain.team.dto.TeamTableDto;
import likelion.mlb.backendProject.domain.team.entity.Team;
import likelion.mlb.backendProject.domain.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/getTable")
    public ResponseEntity<List<TeamTableDto>> getTeamTable() {
        List<TeamTableDto> teamTable = teamService.getTeamTable();
        return new ResponseEntity<>(teamTable, HttpStatus.OK);
    }
}
