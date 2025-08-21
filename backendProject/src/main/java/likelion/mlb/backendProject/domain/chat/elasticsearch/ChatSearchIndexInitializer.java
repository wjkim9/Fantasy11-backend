package likelion.mlb.backendProject.domain.chat.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSearchIndexInitializer {

  public static final String INDEX = "chat_messages_v1";

  private final ElasticsearchClient client;

  @PostConstruct
  public void createIndexIfAbsent() {
    try {
      boolean exists = client.indices().exists(e -> e.index(INDEX)).value();
      if (exists) {
        log.info("[ES] index '{}' already exists", INDEX);
        return;
      }

      // 파일명 혼용 대비: 둘 다 시도
      String settings = readClasspathFirst(
          List.of("elasticsearch/chat-messages-settings.json",
              "elasticsearch/chat-message-settings.json")  // 과거 명칭 대비
      );
      String mappings = readClasspathFirst(
          List.of("elasticsearch/chat-messages-mappings.json",
              "elasticsearch/chat-message-mappings.json")
      );

      String body = "{\"settings\":" + settings + ",\"mappings\":" + mappings + "}";
      client.indices().create(c -> c.index(INDEX).withJson(new StringReader(body)));

      log.info("[ES] index '{}' created successfully", INDEX);

    } catch (IOException e) {
      // nori 미설치 시 흔한 메시지 힌트 추가
      String msg = e.getMessage();
      if (msg != null && (msg.contains("nori") || msg.contains("nori_tokenizer"))) {
        log.error("[ES] analysis-nori 플러그인이 없어 인덱스 생성에 실패했습니다. 컨테이너에서 "
                  + "`elasticsearch-plugin install --batch analysis-nori` 실행 후 재기동하세요.");
      }
      throw new IllegalStateException("Failed to create ES index: " + INDEX, e);
    }
  }

  private static String readClasspathFirst(List<String> candidates) throws IOException {
    for (String path : candidates) {
      ClassPathResource res = new ClassPathResource(path);
      if (res.exists()) {
        try (InputStream in = res.getInputStream()) {
          return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
      }
    }
    throw new FileNotFoundException("Classpath resource not found. tried=" + candidates);
  }
}
