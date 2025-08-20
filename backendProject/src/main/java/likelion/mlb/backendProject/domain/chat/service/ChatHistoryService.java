package likelion.mlb.backendProject.domain.chat.service;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatHistoryPage;


public interface ChatHistoryService {
  /** 최신 페이지(커서 없을 때) */
  ChatHistoryPage loadLatest(UUID roomId, int limit);

  /** 커서 이전 페이지(무한스크롤 위로) */
  ChatHistoryPage loadBefore(UUID roomId, String cursor, int limit);
}