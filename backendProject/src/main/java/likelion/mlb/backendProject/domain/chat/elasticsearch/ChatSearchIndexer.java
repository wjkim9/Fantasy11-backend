package likelion.mlb.backendProject.domain.chat.elasticsearch;

import static likelion.mlb.backendProject.domain.chat.elasticsearch.ChatSearchIndexInitializer.INDEX;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import java.util.HashMap;
import java.util.Map;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatSearchIndexer {

  private final ElasticsearchClient client;

  public void index(ChatMessage m) {
    try {
      Map<String, Object> doc = new HashMap<>();
      doc.put("id", m.getId().toString());
      doc.put("chatRoomId", m.getChatRoomId().toString());
      doc.put("userId", m.getUserId() != null ? m.getUserId().toString() : null); // null 그대로
      doc.put("type", m.getMessageType().name());  // <-- "messageType"가 아니라 "type"으로 인덱싱
      doc.put("createdAt", m.getCreatedAt().toString());
      doc.put("content", m.getContent());

      client.index(b -> b.index(INDEX).id(m.getId().toString()).document(doc));
    } catch (Exception ignore) {

    }
  }
}
