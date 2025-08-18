package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.time.Instant;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.elasticsearch.ChatSearchIndexer;
import likelion.mlb.backendProject.domain.chat.elasticsearch.event.ChatMessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import likelion.mlb.backendProject.domain.chat.entity.ChatMessage;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatMessageService;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ApplicationEventPublisher publisher;

  @Override
  public ChatMessage saveUserMessage(UUID roomId, UUID senderUserId, String content) {
    ChatMessage saved = chatMessageRepository.save(
        ChatMessage.builder()
            .chatRoomId(roomId)
            .userId(senderUserId)
            .messageType(ChatMessage.MessageType.USER)
            .content(content)
            .createdAt(Instant.now())
            .build()
    );
    publisher.publishEvent(new ChatMessageCreatedEvent(saved.getId()));
    return saved;
  }

  @Override
  public ChatMessage saveSystemAlert(UUID roomId, String content) {
    ChatMessage saved = chatMessageRepository.save(
        ChatMessage.builder()
            .chatRoomId(roomId)
            .userId(null)
            .messageType(ChatMessage.MessageType.ALERT)
            .content(content)
            .createdAt(Instant.now())
            .build()
    );
    publisher.publishEvent(new ChatMessageCreatedEvent(saved.getId()));
    return saved;
  }
}