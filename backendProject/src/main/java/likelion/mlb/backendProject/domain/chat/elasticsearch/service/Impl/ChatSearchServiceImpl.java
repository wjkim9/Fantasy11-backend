package likelion.mlb.backendProject.domain.chat.elasticsearch.service.Impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.elasticsearch.dto.ChatSearchResult;
import likelion.mlb.backendProject.domain.chat.elasticsearch.service.ChatSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatSearchServiceImpl implements ChatSearchService {

  private final ElasticsearchClient client;
  private static final String INDEX = "chat-messages";

  @Override
  public ChatSearchResult search(UUID roomId, String q, String cursor, int limit) {
    try {
      List<FieldValue> searchAfter = decodeCursor(cursor); // [createdAt(desc), _id(desc)]

      var resp = client.search(s -> {
        s.index(INDEX)
            .size(limit)
            .trackTotalHits(t -> t.enabled(false))
            .query(qb -> qb.bool(b -> b
                .filter(f -> f.term(t -> t.field("chatRoomId").value(roomId.toString())))
                .must(m -> m.match(mm -> mm.field("content").query(q)))
            ))
            // createdAt DESC, _id DESC 와 동일한 정렬순서로 정의해야 search_after가 동작
            .sort(so -> so.field(f -> f.field("createdAt").order(SortOrder.Desc)))
            .sort(so -> so.field(f -> f.field("_id").order(SortOrder.Desc)));
        if (searchAfter != null) s.searchAfter(searchAfter);
        return s;
      }, Map.class);

      var hits = resp.hits().hits();

      String next = null;
      boolean hasMore = hits.size() == limit;
      if (!hits.isEmpty()) {
        var last = hits.get(hits.size() - 1);
        String createdAt = (String)((Map<?,?>) last.source()).get("createdAt");
        next = encodeCursor(createdAt, last.id());
      }

      var items = hits.stream().map(h -> {
        @SuppressWarnings("unchecked")
        Map<String,Object> src = (Map<String,Object>) h.source();
        return ChatSearchResult.Item.builder()
            .id(UUID.fromString((String) src.get("id")))
            .chatRoomId(UUID.fromString((String) src.get("chatRoomId")))
            .content((String) src.get("content"))
            .userId("NULL".equals(src.get("userId")) ? null : UUID.fromString((String) src.get("userId")))
            .createdAt(Instant.parse((String) src.get("createdAt")))
            .type((String) src.get("messageType"))
            .build();
      }).toList();

      return ChatSearchResult.builder().items(items).nextCursor(next).hasMore(hasMore).build();
    } catch (Exception e) {
      throw new RuntimeException("Chat search failed", e);
    }
  }

  private String encodeCursor(String isoTime, String id) {
    String raw = isoTime + "|" + id;
    return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  // cursor -> List<FieldValue> 로 변경 (ES v8 API 호환)
  private List<FieldValue> decodeCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
    String[] parts = raw.split("\\|", 2);
    // createdAt(ISO8601), _id(String) 순서. 정렬 정의와 동일 순서여야 함.
    return List.of(FieldValue.of(parts[0]), FieldValue.of(parts[1]));
  }
}
