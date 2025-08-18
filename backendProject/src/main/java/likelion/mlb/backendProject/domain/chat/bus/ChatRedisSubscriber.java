package likelion.mlb.backendProject.domain.chat.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;
  private final ChatRedisPublisher publisher;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String channel = new String(message.getChannel());
    String body = new String(message.getBody());
    try {
      @SuppressWarnings("unchecked")
      Map<String,Object> payload = objectMapper.readValue(body, Map.class);
      if (publisher.nodeId().equals(payload.get("_src"))) return; // 내가 보낸 건 무시
      String topic = ChatChannels.toTopic(channel);
      messagingTemplate.convertAndSend(topic, payload);
    } catch (Exception ignore) {}
  }
}