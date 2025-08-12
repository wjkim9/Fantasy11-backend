package likelion.mlb.backendProject.global.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class FplConfig {

    @Bean(name = "fplApiWebClient")
    public WebClient fplApiWebClient(
            WebClient.Builder builder,
            @Value("${fpl.base-url:https://fantasy.premierleague.com/api}") String baseUrl,
            @Value("${fpl.max-in-memory-mb:10}") int maxMb
    ) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(maxMb * 1024 * 1024))
                .build();

        return builder
                .exchangeStrategies(strategies)
                .baseUrl(baseUrl) // ← baseUrl은 여기서 설정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
