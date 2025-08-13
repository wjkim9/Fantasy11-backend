package likelion.mlb.backendProject.global.configuration;

import likelion.mlb.backendProject.global.staticdata.dto.bootstrap.BootstrapStatic;
import likelion.mlb.backendProject.global.staticdata.dto.fixture.FplFixture;
import likelion.mlb.backendProject.global.staticdata.dto.live.LiveEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FplWebClient implements FplClient{
    private final WebClient webClient;        // 기존 WebConfig @Bean 재사용
    private String baseUrl = "https://fantasy.premierleague.com/api";

    @Override
    public BootstrapStatic getBootstrapStatic() {
        return webClient.get().uri(baseUrl + "/bootstrap-static")
                .retrieve().bodyToMono(BootstrapStatic.class).block();
    }

    @Override
    public List<FplFixture> getFixtures(int event) {
        return webClient.get()
                .uri(u -> u.path("/fixtures")              // ✅ baseUrl 없이 상대 경로
                        .queryParam("event", event)
                        .build())
                .accept(MediaType.APPLICATION_JSON)        // (선택) 응답 JSON 기대 명시
                .retrieve()
                .bodyToFlux(FplFixture.class)
                .collectList()
                .block();
    }

    @Override
    public LiveEventDto getLive(int round) {
        return webClient.get().uri(baseUrl + "/event/{round}/live", round)
                .retrieve().bodyToMono(LiveEventDto.class).block();
    }
}
