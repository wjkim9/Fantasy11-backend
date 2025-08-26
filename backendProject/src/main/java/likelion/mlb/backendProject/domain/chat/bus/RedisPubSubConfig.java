package likelion.mlb.backendProject.domain.chat.bus;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

//@Configuration  // Redis 방식 비활성화
@RequiredArgsConstructor
public class RedisPubSubConfig {

  private final RedisConnectionFactory connectionFactory;
  private final ChatRedisSubscriber subscriber;

  @Bean(name = "redisChatMessageListenerContainer")
  public RedisMessageListenerContainer redisMessageListenerContainer() {
    var container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(subscriber, new PatternTopic("chat:room:*"));
    return container;
  }
}