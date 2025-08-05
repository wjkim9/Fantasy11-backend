package likelion.mlb.backendProject.global.runner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FplConfig {

    @Bean
    public WebClient fplWebClient(WebClient.Builder builder) {

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs()
                                // 10MB 까지 버퍼링 허용
                                .maxInMemorySize(10 * 1024 * 1024)
                )
                .build();

        return builder
                .exchangeStrategies(strategies)
                .baseUrl("https://fantasy.premierleague.com/api")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
