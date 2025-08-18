package likelion.mlb.backendProject.domain.chat.elasticsearch.service;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.elasticsearch.dto.ChatSearchResult;

public interface ChatSearchService {
  ChatSearchResult search(UUID roomId, String q, String cursor, int limit);
}