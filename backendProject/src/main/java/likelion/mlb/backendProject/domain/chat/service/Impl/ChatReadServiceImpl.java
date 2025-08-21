package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.entity.ChatReadState;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.repository.ChatReadStateRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatReadService;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatReadServiceImpl implements ChatReadService {

  private final ChatReadStateRepository readRepo;
  private final ChatMessageRepository messageRepo;

  @Override
  @Transactional(readOnly = true)
  public Optional<UUID> getLastReadMessageId(UUID roomId, UUID userId) {
    return readRepo.findByChatRoomIdAndUserId(roomId, userId).map(ChatReadState::getLastMessageId);
  }

  @Override
  public void markReadUpTo(UUID roomId, UUID userId, UUID messageId) {
    ChatReadState state = readRepo.findByChatRoomIdAndUserId(roomId, userId)
        .orElseGet(() -> ChatReadState.builder()
            .chatRoomId(roomId)
            .userId(userId)
            .lastReadAt(Instant.EPOCH)
            .build());

    Instant when = Instant.now();
    if (messageId != null) {
      ChatMessage msg = messageRepo.findById(messageId).orElse(null);
      if (msg != null && roomId.equals(msg.getChatRoomId())) {
        when = msg.getCreatedAt(); // 메시지 시각까지로 마킹
      }
    }
    state.mark(messageId, when);
    readRepo.save(state);
  }

  @Override
  @Transactional(readOnly = true)
  public long countUnread(UUID roomId, UUID userId) {
    Instant since = readRepo.findByChatRoomIdAndUserId(roomId, userId)
        .map(ChatReadState::getLastReadAt)
        .orElse(Instant.EPOCH);
    return readRepo.countUnread(roomId, since);
  }
}
