package likelion.mlb.backendProject.domain.chat.elasticsearch.service;

import static likelion.mlb.backendProject.domain.chat.elasticsearch.ChatSearchIndexInitializer.INDEX;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.Base64;
import likelion.mlb.backendProject.domain.chat.elasticsearch.dto.ChatSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSearchService {

  private final ElasticsearchClient client;

  /**
   * 부분문자열 + 풀텍스트 검색 (정렬: createdAt desc, id desc)
   * cursor = base64("epochMillis|uuid")
   */
  public ChatSearchResult search(UUID roomId, String q, String cursor, int limit) {
    limit = Math.max(1, Math.min(limit, 50));

    try {
      // 1) bool 쿼리
      List<Query> must = new ArrayList<>();
      must.add(Query.of(t -> t.term(tt -> tt.field("chatRoomId").value(roomId.toString()))));
      if (q != null && !q.isBlank()) {
        must.add(Query.of(m -> m.match(mm -> mm.field("content").query(q))));
      }
      Query bool = Query.of(b -> b.bool(bb -> bb.must(must)));

      // 2) 정렬 + 커서(search_after)
      List<SortOptions> sort = List.of(
          SortOptions.of(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc))),
          SortOptions.of(s -> s.field(f -> f.field("id").order(SortOrder.Desc)))
      );

      List<FieldValue> searchAfter = parseCursor(cursor);

      // 3) 요청 빌드
      SearchRequest.Builder sb = new SearchRequest.Builder()
          .index(INDEX)
          .query(bool)
          .sort(sort)
          .size(limit);
      if (searchAfter != null) sb.searchAfter(searchAfter);

      // 4) 실행
      SearchResponse<Map> res = client.search(sb.build(), Map.class);

      // 5) 결과 변환
      List<ChatSearchResult.Item> items = new ArrayList<>();
      for (Hit<Map> h : res.hits().hits()) {
        Map src = h.source();
        if (src == null) continue;

        String id = (String) src.get("id");
        String chatRoomId = (String) src.get("chatRoomId");
        String userId = (String) src.get("userId"); // null 가능
        String type = (String) src.get("type");
        String createdAt = (String) src.get("createdAt");
        String content = (String) src.get("content");

        items.add(ChatSearchResult.Item.builder()
            .id(UUID.fromString(id))
            .chatRoomId(UUID.fromString(chatRoomId))
            .userId(userId != null ? UUID.fromString(userId) : null)
            .type(type)
            .createdAt(Instant.parse(createdAt))
            .content(content)
            .build());
      }

      // 6) 커서 생성 (도메인 값 기반: 안전하고 단순)
      boolean hasMore = items.size() == limit;
      String nextCursor = null;
      if (hasMore) {
        ChatSearchResult.Item last = items.get(items.size() - 1);
        long millis = last.getCreatedAt().toEpochMilli();
        String raw = millis + "|" + last.getId();
        nextCursor = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
      }

      return ChatSearchResult.builder()
          .items(items)
          .hasMore(hasMore)
          .nextCursor(nextCursor)
          .build();

    } catch (IOException e) {
      log.error("[chat-search] ES search failed. roomId={}, q={}, cursor={}, limit={}",
          roomId, q, cursor, limit, e);
      return ChatSearchResult.builder()
          .items(List.of())
          .hasMore(false)
          .nextCursor(null)
          .build();
    }
  }

  /** cursor(base64 "millis|uuid") → search_after([millis, uuid]) */
  private static List<FieldValue> parseCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    try {
      String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
      String[] parts = decoded.split("\\|", 2);
      long millis = Long.parseLong(parts[0]);
      String lastId = parts[1];
      return List.of(FieldValue.of(millis), FieldValue.of(lastId));
    } catch (Exception ignore) {
      return null; // 잘못된 커서는 무시하고 첫 페이지로
    }
  }
}
