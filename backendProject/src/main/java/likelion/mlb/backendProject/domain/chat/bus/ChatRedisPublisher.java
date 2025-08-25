package likelion.mlb.backendProject.domain.chat.bus;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRedisPublisher {
  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper;
  // JVM 런타임 식별자를 사용하여 노드 구분 (재시작 시마다 변경됨)
  private final String nodeId = System.getProperty("node.id", 
      java.lang.management.ManagementFactory.getRuntimeMXBean().getName());

  public void publishToRoom(String roomId, Map<String, Object> payload) {
    try {
      payload.put("_src", nodeId); // 루프 방지 태그
      String json = objectMapper.writeValueAsString(payload);
      stringRedisTemplate.convertAndSend(roomId, json);
    } catch (Exception ignore) {}
  }

  public String nodeId() { return nodeId; }
}