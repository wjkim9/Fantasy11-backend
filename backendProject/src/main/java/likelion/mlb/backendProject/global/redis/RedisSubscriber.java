package likelion.mlb.backendProject.global.redis;


import com.fasterxml.jackson.databind.ObjectMapper;

import likelion.mlb.backendProject.domain.draft.dto.DraftRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    @Lazy
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        try {
            String msgBody = new String(message.getBody());
            DraftRequest draftRequest = objectMapper.readValue(msgBody, DraftRequest.class);

            simpMessagingTemplate.convertAndSend("/topic/draft." + draftRequest.getDraftId(), draftRequest);
        } catch (Exception e) {

        }

    }
}
