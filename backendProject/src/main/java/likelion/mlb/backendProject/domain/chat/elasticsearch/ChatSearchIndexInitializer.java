package likelion.mlb.backendProject.domain.chat.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import java.io.StringReader;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatSearchIndexInitializer {

  private final ElasticsearchClient client;
  private static final String INDEX = "chat-messages";

  @PostConstruct
  public void createIndexIfAbsent() throws Exception {
    var exists = client.indices().exists(e -> e.index(INDEX)).value();
    if (!exists) {
      // settings
      String settings = new String(
          Objects.requireNonNull(getClass().getResourceAsStream("/elasticsearch/chat-message-settings.json"))
              .readAllBytes());
      // mappings
      String mappings = new String(
          Objects.requireNonNull(getClass().getResourceAsStream("/elasticsearch/chat-message-mappings.json"))
              .readAllBytes());

      client.indices().create(b -> b.index(INDEX)
          .withJson(new StringReader("{\"settings\":" + settings + ",\"mappings\":" + mappings + "}"))
      );
    }
  }
}
