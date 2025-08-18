package likelion.mlb.backendProject.domain.chat.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import java.util.Map;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatSearchIndexer {

  private final ElasticsearchClient client;
  private static final String INDEX = "chat-messages";

  public void index(ChatMessage m) {
    try {
      client.index(b -> b.index(INDEX)
          .id(m.getId().toString())
          .document(Map.of(
              "id", m.getId().toString(),
              "chatRoomId", m.getChatRoomId().toString(),
              "userId", m.getUserId() == null ? "NULL" : m.getUserId().toString(),
              "messageType", m.getMessageType().name(),
              "createdAt", m.getCreatedAt().toString(),
              "content", m.getContent()
          )));
    } catch (Exception e) {
      // 실패해도 채팅은 계속되게: 로그만 남기고 복구 배치에서 재색인
      // log.warn("Index failed for message {}", m.getId(), e);
    }
  }
}
