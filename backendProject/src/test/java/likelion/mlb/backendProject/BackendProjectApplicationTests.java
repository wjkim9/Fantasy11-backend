package likelion.mlb.backendProject;

import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;
import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import likelion.mlb.backendProject.domain.team.repository.TeamRepository;
import likelion.mlb.backendProject.global.configuration.FplClient;
import likelion.mlb.backendProject.global.scheduler.service.DataUpdaterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(properties = {
		"spring.task.scheduling.enabled=false", // 스케줄러 비활성화(권장)
		"spring.data.elasticsearch.repositories.enabled=false",
		"spring.cache.type=none", // 캐시 매니저 NoOp로
		"spring.autoconfigure.exclude=" +
				"org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
				"org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration," +
				"org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
@ActiveProfiles("test")
class BackendProjectApplicationTests {

	@Autowired
	DataUpdaterService service;

	@MockitoBean
	FplClient fplClient;
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
