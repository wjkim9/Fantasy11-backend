package likelion.mlb.backendProject.domain.draft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import likelion.mlb.backendProject.domain.draft.dto.DraftParticipant;
import likelion.mlb.backendProject.domain.draft.dto.DraftRequest;
import likelion.mlb.backendProject.domain.draft.dto.DraftResponse;
import likelion.mlb.backendProject.domain.draft.service.DraftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/{participantId}/players")
    @ResponseBody
    public List<DraftResponse> getPlayersByParticipantId(@PathVariable("participantId") UUID participantId) {
        return draftService.getPlayersByParticipantId(participantId);
    }


    /*
     * 드래프트 방 입장 시 해당 드래프트 방에 속해 있는 4명의 참여자 리스트 가져오기
     */
    @GetMapping("/{draftId}/participants")
    @ResponseBody
    public List<DraftParticipant> getParticipantsByDraftId(@PathVariable("draftId") UUID draftId) {
        return draftService.getParticipantsByDraftId(draftId);
    }

    /*
     * 해당 드래프트 방에서 선택 된 선수 리스트 가져오기
     */
    @GetMapping("/{draftId}/allPlayers")
    @ResponseBody
    public List<DraftResponse> getAllPlayersByDraftId(@PathVariable("draftId") UUID draftId) {
        return draftService.getAllPlayersByDraftId(draftId);
    }

    /*
     * isWithinSquadLimits테스트용
     */
    @GetMapping("squadLimit")
    @ResponseBody
    public boolean isWithinSquadLimits(@RequestParam(value="participantId") UUID participantId,
                                                      @RequestParam(value="participantId")UUID elementTypeId) {
        return draftService.isWithinSquadLimitsTest(participantId, elementTypeId);
    }
}
