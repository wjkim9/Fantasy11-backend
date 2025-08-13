package likelion.mlb.backendProject;

import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.team.repository.TeamRepository;
import likelion.mlb.backendProject.global.scheduler.service.DataUpdaterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest
class BackendProjectApplicationTests {

	@Autowired
	DataUpdaterService service;

	@Autowired
	@MockitoBean
	WebClient fplClient;
	@Autowired
	TeamRepository teamRepo;
	@Autowired
	PlayerRepository playerRepo;
	@Autowired
	RoundRepository roundRepo;

	@Test
	void contextLoads() {
	}

}
