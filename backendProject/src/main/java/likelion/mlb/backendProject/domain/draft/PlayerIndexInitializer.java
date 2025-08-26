package likelion.mlb.backendProject.domain.draft;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerIndexInitializer {

    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void createIndexIfAbsent() {
        String indexName = "player-index";

        try {
            // 인덱스 존재 여부 확인
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(indexName))
                    .value();

            if (!exists) {
                log.info("Index [{}] not found. Creating...", indexName);

                // classpath에서 JSON 파일 불러오기
                ClassPathResource resource = new ClassPathResource("elasticsearch/player-index.json");
                String json = Files.readString(resource.getFile().toPath());

                // 인덱스 생성 요청
                elasticsearchClient.indices().create(
                        CreateIndexRequest.of(c -> c
                                .index(indexName)
                                .withJson(new java.io.StringReader(json))
                        )
                );

                log.info("Index [{}] created successfully.", indexName);
            } else {
                log.info("Index [{}] already exists.", indexName);
            }
        } catch (IOException e) {
            log.error("Failed to check or create index [{}]", indexName, e);
        }
    }
}