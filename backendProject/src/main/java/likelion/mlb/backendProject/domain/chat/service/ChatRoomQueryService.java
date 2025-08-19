package likelion.mlb.backendProject.domain.chat.service;

import java.util.List;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.RosterResponse;
import likelion.mlb.backendProject.domain.chat.dto.ScoreboardItem;

public interface ChatRoomQueryService {

  /** 방 스코어보드(4명, 퍼포먼스 합계 기준 랭킹) */
  List<ScoreboardItem> getScoreboard(UUID roomId);

  /** 특정 참가자의 로스터(11인) + 포메이션 계산 */
  RosterResponse getRoster(UUID roomId, UUID participantId);
}