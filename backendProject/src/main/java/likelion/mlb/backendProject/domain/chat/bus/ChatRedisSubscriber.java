package likelion.mlb.backendProject.domain.chat.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
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

  @Override
  public void onMessage(Message message, byte[] pattern) {
    // 채널/바디는 UTF-8로 디코드
    String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
    String body    = new String(message.getBody(), StandardCharsets.UTF_8);
    try {
      @SuppressWarnings("unchecked")
      Map<String, Object> payload = objectMapper.readValue(body, Map.class);

      // ✅ 내 노드에서 보낸 메시지는 이미 전달했으므로 Redis를 통한 재전달 방지
      // (ChatRedisPublisher에서 nodeId를 _src로 설정)
      Object srcNodeId = payload.get("_src");
      if (srcNodeId != null) {
        // 현재 노드의 ID와 비교 (ChatRedisPublisher의 nodeId와 비교하려면 주입받아야 함)
        // 여기서는 간단히 현재 JVM의 식별자로 비교
        String currentNodeId = System.getProperty("node.id", 
            java.lang.management.ManagementFactory.getRuntimeMXBean().getName());
        if (currentNodeId.equals(srcNodeId)) {
          return; // 내가 보낸 메시지는 중복 전송 방지
        }
      }

      String topic = ChatChannels.toTopic(channel);
      // ✅ STOMP 브로커로 전송 (다른 노드에서 온 메시지만)
      messagingTemplate.convertAndSend(topic, payload);

    } catch (Exception e) {
      // 필요하면 로그 추가
      // log.warn("Failed to handle chat redis msg: {}", body, e);
    }
  }
}
