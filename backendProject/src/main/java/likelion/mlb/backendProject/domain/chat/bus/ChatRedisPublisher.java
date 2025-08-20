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
  private final String nodeId = java.util.UUID.randomUUID().toString();

  public void publishToRoom(UUID roomId, Map<String, Object> payload) {
    try {
      payload.put("_src", nodeId); // 루프 방지 태그
      String json = objectMapper.writeValueAsString(payload);
      stringRedisTemplate.convertAndSend(ChatChannels.roomChannel(roomId), json);
    } catch (Exception ignore) {}
  }

  public String nodeId() { return nodeId; }
}