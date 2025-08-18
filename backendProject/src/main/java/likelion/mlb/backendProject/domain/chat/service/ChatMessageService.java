package likelion.mlb.backendProject.domain.chat.service;

import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;

public interface ChatMessageService {

  /** 사용자 메시지 저장 */
  ChatMessage saveUserMessage(UUID roomId, UUID senderUserId, String content);

  /** 시스템/알림 메시지 저장 */
  ChatMessage saveSystemAlert(UUID roomId, String content);
}
