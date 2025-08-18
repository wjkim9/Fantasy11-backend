package likelion.mlb.backendProject.domain.chat.service;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;

public interface ChatRoomService {

  /**
   * 드래프트 생성 시 1줄로 호출: 없으면 만들고, 있으면 반환
   */
  ChatRoom createForDraft(UUID draftId);

  /**
   * 드래프트로 채팅방 ID 조회
   */
  UUID getRoomIdByDraft(UUID draftId);
}