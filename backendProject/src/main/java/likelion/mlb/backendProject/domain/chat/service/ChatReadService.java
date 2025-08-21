package likelion.mlb.backendProject.domain.chat.service;


import java.util.Optional;
import java.util.UUID;

public interface ChatReadService {
  /** 마지막 읽음 커서 반환 (없으면 empty) */
  Optional<UUID> getLastReadMessageId(UUID roomId, UUID userId);

  /** messageId까지 읽음 처리 (null이면 '지금 시각' 기준으로만 마킹) */
  void markReadUpTo(UUID roomId, UUID userId, UUID messageId);

  /** 미읽음 개수 */
  long countUnread(UUID roomId, UUID userId);
}
