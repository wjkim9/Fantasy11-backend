package likelion.mlb.backendProject.domain.chat.elasticsearch;

import likelion.mlb.backendProject.domain.chat.elasticsearch.event.ChatMessageCreatedEvent;
import likelion.mlb.backendProject.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatMessageIndexListener {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatSearchIndexer chatSearchIndexer;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(ChatMessageCreatedEvent e) {
    var msg = chatMessageRepository.getReferenceById(e.messageId());
    chatSearchIndexer.index(msg); // 여기서 ES 색인
  }
}