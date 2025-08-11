package likelion.mlb.backendProject.domain.draft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import likelion.mlb.backendProject.domain.draft.dto.DraftRequest;
import likelion.mlb.backendProject.domain.draft.service.DraftService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

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
    public void selectPlayer(DraftRequest draftRequest
            , Principal principal
    ) throws JsonProcessingException {

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
}
