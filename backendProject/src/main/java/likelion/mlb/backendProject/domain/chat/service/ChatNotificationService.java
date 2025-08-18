package likelion.mlb.backendProject.domain.chat.service;

import java.util.UUID;
import likelion.mlb.backendProject.domain.player.entity.live.MatchEvent;

public interface ChatNotificationService {
  /** LiveDataService에서 MatchEvent 저장 직후 호출하는 메서드 */
  void sendMatchAlert(MatchEvent event);

  /** 테스트/임시 호출용(원시 파라미터) */
  void sendMatchAlert(UUID playerId, UUID fixtureId, String eventType, Integer minute, Integer point, String text);
}
