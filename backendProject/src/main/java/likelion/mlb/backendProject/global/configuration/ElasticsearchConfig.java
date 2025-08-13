package likelion.mlb.backendProject.global.configuration;

import jakarta.annotation.PostConstruct;
import likelion.mlb.backendProject.domain.chat.elasticsearch.ChatMessageDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig {

  private final ElasticsearchOperations esOps;

  @PostConstruct
  public void createChatIndex() {
    IndexOperations ops = esOps.indexOps(ChatMessageDocument.class);
    if (!ops.exists()) {
      ops.createWithMapping();
    }
  }
}

