package likelion.mlb.backendProject.domain.chat.service.Impl;


import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;
import likelion.mlb.backendProject.domain.chat.dto.ChatHistoryPage;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageQueryRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatHistoryServiceImpl implements ChatHistoryService {

  private final ChatMessageQueryRepository queryRepo;

  private static final Sort CHAT_SORT =
      Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

  @Override
  public ChatHistoryPage loadLatest(UUID roomId, int limit) {
    // limit 방어 (음수/0/과도한 값)
    int size = Math.max(1, Math.min(limit, 100));
    Pageable pageable = PageRequest.of(0, size, CHAT_SORT);
    var rows = queryRepo.findRecent(roomId, pageable);
    return buildPage(rows, size);
  }

  @Override
  public ChatHistoryPage loadBefore(UUID roomId, String cursor, int limit) {
    int size = Math.max(1, Math.min(limit, 100));
    if (cursor == null || cursor.isBlank()) return loadLatest(roomId, size);

    var c = decodeCursor(cursor); // createdAt + id
    Pageable pageable = PageRequest.of(0, size, CHAT_SORT);
    var rows = queryRepo.findBefore(roomId, c.createdAt(), c.id(), pageable);
    return buildPage(rows, size);
  }

  // DB는 created_at DESC로 뽑으므로, 프론트 바인딩 편의를 위해 오름차순으로 뒤집어 반환
  private ChatHistoryPage buildPage(List<ChatMessage> rowsDesc, int limit) {
    boolean hasMore = rowsDesc.size() == limit;
    // 다음 커서는 "가장 오래된(=rowsDesc 마지막)" 기준
    String nextCursor = rowsDesc.isEmpty() ? null :
        encodeCursor(rowsDesc.get(rowsDesc.size() - 1).getCreatedAt(),
            rowsDesc.get(rowsDesc.size() - 1).getId());

    // 오름차순 변환
    var items = new ArrayList<>(rowsDesc);
    Collections.reverse(items);

    return ChatHistoryPage.builder()
        .items(items.stream().map(m ->
            ChatHistoryPage.Item.builder()
                .id(m.getId())
                .chatRoomId(m.getChatRoomId())
                .type(m.getMessageType().name())
                .content(m.getContent())
                .userId(m.getUserId())
                .createdAt(m.getCreatedAt())
                .build()
        ).collect(Collectors.toList()))
        .nextCursor(nextCursor)
        .hasMore(hasMore)
        .build();
  }

  // cursor = base64("epochMillis:id")
  private String encodeCursor(Instant createdAt, UUID id) {
    String raw = createdAt.toEpochMilli() + ":" + id.toString();
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private Cursor decodeCursor(String cursor) {
    String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
    String[] parts = raw.split(":");
    return new Cursor(Instant.ofEpochMilli(Long.parseLong(parts[0])), UUID.fromString(parts[1]));
  }

  private record Cursor(Instant createdAt, UUID id) {}
}