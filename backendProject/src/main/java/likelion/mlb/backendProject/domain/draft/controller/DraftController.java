package likelion.mlb.backendProject.domain.draft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import likelion.mlb.backendProject.domain.draft.dto.DraftRequest;
import likelion.mlb.backendProject.domain.draft.entity.ParticipantPlayer;
import likelion.mlb.backendProject.domain.draft.handler.StompPrincipal;
import likelion.mlb.backendProject.domain.draft.repository.ParticipantPlayerRepository;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.domain.redis.RedisPublisher;
import likelion.mlb.backendProject.global.staticdata.entity.Player;
import likelion.mlb.backendProject.global.staticdata.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.*;

/**
 * 드래프트 관련 controller
 * */
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/draft")
@Slf4j
public class DraftController {

    // redisPublisher
    private final RedisPublisher redisPublisher;

    // json<->dto 를 위한 객체
    private final ObjectMapper objectMapper;

    private final ParticipantRepository participantRepository;
    private final PlayerRepository playerRepository;
    private final ParticipantPlayerRepository participantPlayerRepository;


    /*
     * (방이 없을 시)동적으로 방 생성 및 채팅
     */
    @MessageMapping("/draft/selectPlayer")
    public void sendmessage(DraftRequest draftRequest
            , Principal principal
    ) throws JsonProcessingException {

        String channel = null;
        String msg = null;
//        UUID participantId = ((StompPrincipal) principal).getParticipantId(); // 현 로그인 한 사용자 member pk

        // 일반 메시지
        channel = "room."+draftRequest.getDraftId();
        msg = objectMapper.writeValueAsString(draftRequest);

        redisPublisher.publish(channel, msg);

        try {
            // DB에 메시지 저장
//            saveDraft(draftRequest);
        } catch (RuntimeException e) {
            log.error(" 드래프트 postgreSql 저장 실패 : {}", e.getMessage());

            throw e;
        }
    }

    /*
     *  DB에 메시지 저장
     **/
    private void saveDraft(DraftRequest draftRequest) {
        UUID participantId = draftRequest.getParticipantId(); //
        UUID playerId = draftRequest.getPlayerId(); //

        //
        Participant participant = participantRepository.findById(participantId).orElseThrow(() ->
                new RuntimeException());

        //
        Player player = playerRepository.findById(playerId).orElseThrow(()
                -> new RuntimeException());

        participantPlayerRepository.save(ParticipantPlayer.builder()
                .participant(participant)
                .player(player)
                .build());
    }

}
