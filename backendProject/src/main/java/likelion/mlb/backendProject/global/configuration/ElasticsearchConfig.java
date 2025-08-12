package likelion.mlb.backendProject.global.configuration;

import jakarta.annotation.PostConstruct;
import likelion.mlb.backendProject.domain.chat.elasticsearch.ChatMessageDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig {

  private final ElasticsearchOperations esOps;

  @PostConstruct
  public void createChatIndex() {
    var ops = esOps.indexOps(ChatMessageDocument.class);
    if (!ops.exists()) {
      ops.createWithMapping();
    }
  }
}

