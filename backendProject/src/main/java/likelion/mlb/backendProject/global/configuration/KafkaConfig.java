package likelion.mlb.backendProject.global.configuration;

import java.util.HashMap;
import java.util.Map;
import likelion.mlb.backendProject.domain.chat.event.ChatMessageEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaConfig {

  @Bean
  public ProducerFactory<String, ChatMessageEvent> producerFactory() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
        "${spring.kafka.bootstrap-servers}");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        StringSerializer.class);
    // JsonSerializer 설정: ChatMessageEvent를 자동 직렬화
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        JsonSerializer.class);
    // 타입 헤더 없이 순수 JSON만 보내고 싶으면 아래 옵션 추가
    props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

    return new DefaultKafkaProducerFactory<>(props);
  }

  @Bean
  public KafkaTemplate<String, ChatMessageEvent> kafkaTemplate(
      ProducerFactory<String, ChatMessageEvent> pf) {
    return new KafkaTemplate<>(pf);
  }
}
