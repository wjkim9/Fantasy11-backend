package likelion.mlb.backendProject.domain.draft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import likelion.mlb.backendProject.domain.draft.dto.DraftRequest;
import likelion.mlb.backendProject.domain.draft.dto.DraftResponse;
import likelion.mlb.backendProject.domain.draft.service.DraftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private final DraftService draftService;

    /*
     * 선수 드래프트 웹소켓 통신
     */
    @MessageMapping("/draft/selectPlayer")
    public void selectPlayer(@Payload DraftRequest draftRequest, Principal principal) throws JsonProcessingException {

        try {
            draftService.selectPlayer(draftRequest, principal);
        } catch (RuntimeException e) {
            log.error(" 드래프트 postgreSql 저장 실패 : {}", e.getMessage());

            throw e;
        } catch (JsonProcessingException e) {
            log.error(" 드래프트 postgreSql 저장 실패 : {}", e.getMessage());

            throw e;
        }
    }

    /*
    * 참여자(participant)클릭 시 해당 참여자가 선택한 선수 리스트 가져오기
    * */
    @GetMapping("/players/{participantId}")
    public List<DraftResponse> getPlayersByParticipantId(@PathVariable("participantId") UUID participantId) {
        return draftService.getPlayersByParticipantId(participantId);
    }
}
