package likelion.mlb.backendProject.domain.draft.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import likelion.mlb.backendProject.domain.draft.dto.DraftRequest;
import likelion.mlb.backendProject.domain.draft.dto.DraftResponse;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.draft.entity.ParticipantPlayer;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.domain.draft.repository.ParticipantPlayerRepository;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.repository.PlayerRepository;
import likelion.mlb.backendProject.domain.user.entity.User;
import likelion.mlb.backendProject.domain.user.repository.UserRepository;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.exception.ErrorCode;
import likelion.mlb.backendProject.global.redis.RedisPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftService {

    // redisPublisher
    private final RedisPublisher redisPublisher;

    // json<->dto 를 위한 객체
    private final ObjectMapper objectMapper;

    private final ParticipantRepository participantRepository;
    private final PlayerRepository playerRepository;
    private final ParticipantPlayerRepository participantPlayerRepository;
    private final UserRepository userRepository;
    private final DraftRepository draftRepository;


    public void selectPlayer(DraftRequest draftRequest
            , java.security.Principal principal
    ) throws JsonProcessingException {

        boolean alreadySelected = participantPlayerRepository // 이미 해당 드래프트방에서 해당 선수가 선택 되었는 지 여부
                .existsByParticipant_Draft_IdAndPlayer_Id(draftRequest.getDraftId(), draftRequest.getPlayerId());

        draftRequest.setAlreadySelected(alreadySelected);


        // participant 객체 추출
        var auth = (org.springframework.security.core.Authentication) principal;
        var cud  = (likelion.mlb.backendProject.global.security.dto.CustomUserDetails) auth.getPrincipal();

//        if (cud.getUser().getId() != null) {
        User user = userRepository.findById(cud.getUser().getId())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));
//        }
        Draft draft = draftRepository.findById(draftRequest.getDraftId()).orElseThrow(() -> new BaseException(ErrorCode.DRAFT_NOT_FOUND));
        Participant participant = participantRepository.findByUserAndDraft(user, draft).orElseThrow(()
                -> new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND));
        draftRequest.setParticipantId(participant.getId());


        String channel = null;
        String msg = null;
//        UUID participantId = ((StompPrincipal) principal).getParticipantId(); // 현 로그인 한 사용자 member pk

        // 일반 메시지
        channel = "room."+draftRequest.getDraftId();
        msg = objectMapper.writeValueAsString(draftRequest);

        redisPublisher.publish(channel, msg);

        try {
            // DB에 참여자와 뽑은 선수 정보 저장
//            saveDraft(draftRequest, participant);
        } catch (RuntimeException e) {
            log.error(" 드래프트 postgreSql 저장 실패 : {}", e.getMessage());

            throw e;
        }
    }

    /*
     *  DB에 참여자와 뽑은 선수 정보 저장
     **/
    private void saveDraft(DraftRequest draftRequest, Participant participant) {
//        UUID participantId = draftRequest.getParticipantId(); //
        UUID playerId = draftRequest.getPlayerId(); //

        //
//        Participant participant = participantRepository.findById(participantId).orElseThrow(() ->
//                new RuntimeException());

        //
        Player player = playerRepository.findById(playerId).orElseThrow(()
                -> new RuntimeException());

        participantPlayerRepository.save(ParticipantPlayer.builder()
                .participant(participant)
                .player(player)
                .build());
    }

    public List<DraftResponse> getPlayersByParticipantId(UUID participantId) {
        List<ParticipantPlayer> playerList = participantPlayerRepository.findByParticipant_Id(participantId);

        return ParticipantPlayer.toDtoList(playerList);
    }

}
