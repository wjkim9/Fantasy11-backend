package likelion.mlb.backendProject.domain.chat.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

//@Component  // Redis 방식 비활성화
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    // 채널/바디는 UTF-8로 디코드
    String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
    String body    = new String(message.getBody(), StandardCharsets.UTF_8);
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> payload = objectMapper.readValue(body, Map.class);

      // ❌ 내 노드에서 보낸 것도 브로커로 전달해야 함 (중복루프 없음)
      // if (publisher.nodeId().equals(payload.get("_src"))) return;

      String topic = ChatChannels.toTopic(channel);
      // ✅ STOMP 브로커로 전송
      messagingTemplate.convertAndSend(topic, payload);

    } catch (Exception e) {
      // 필요하면 로그 추가
      // log.warn("Failed to handle chat redis msg: {}", body, e);
    }
  }
}
