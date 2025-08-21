package likelion.mlb.backendProject.domain.user.service;

import java.util.Optional;
import java.util.UUID;

public interface UserRoomQueryService {
  /** 유저가 가장 최근에 배정된 드래프트의 채팅방 ID 반환 */
  Optional<UUID> findLatestChatRoomForUser(UUID userId);
}