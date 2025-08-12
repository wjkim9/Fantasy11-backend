package likelion.mlb.backendProject.domain.chat.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.mlb.backendProject.domain.chat.dto.ChatMessageDto;
import likelion.mlb.backendProject.domain.chat.elasticsearch.ChatMessageDocument;
import likelion.mlb.backendProject.domain.chat.elasticsearch.ChatMessageSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageIndexer {

  private final ChatMessageSearchRepository searchRepo;
  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  @KafkaListener(topics = "chat-messages", groupId = "chat-indexer")
  public void indexMessage(String payload) throws Exception {

    ChatMessageEvent evt = objectMapper.readValue(payload, ChatMessageEvent.class);


    ChatMessageDocument doc = new ChatMessageDocument();
    doc.setId(evt.getMessageId().toString());
    doc.setRoomId(evt.getRoomId().toString());
    doc.setUserId(evt.getUserId() != null ? evt.getUserId().toString() : null);
    doc.setContent(evt.getContent());
    doc.setMessageType(evt.getMessageType());
    doc.setCreatedAt(evt.getCreatedAt());
    searchRepo.save(doc);


    ChatMessageDto dto = ChatMessageDto.from(evt);
    messagingTemplate.convertAndSend(
        "/topic/chat/" + evt.getRoomId(),
        dto
    );
  }
}
