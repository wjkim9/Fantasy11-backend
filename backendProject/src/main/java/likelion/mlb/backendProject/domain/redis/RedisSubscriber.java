package likelion.mlb.backendProject.domain.redis;


import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.beanBa.backendProject.chatting.dto.ChattingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate simpMessagingTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
            String msgBody = new String(message.getBody());
            ChattingRequest chattingRequest = objectMapper.readValue(msgBody, ChattingRequest.class);

            simpMessagingTemplate.convertAndSend("/topic/room." + chattingRequest.getRoomPk(), chattingRequest);
        } catch (Exception e) {

        }

    }
}
