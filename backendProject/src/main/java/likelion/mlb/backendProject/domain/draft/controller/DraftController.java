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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.security.Principal;
import java.util.*;

/**
 * 드래프트 관련 controller
 * */
@Tag(name = "Draft", description = "드래프트 관리 API")
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

    @Operation(summary = "참여자별 선수 목록 조회", description = "특정 참여자가 선택한 선수 리스트를 조회합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "선수 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "참여자를 찾을 수 없음")
    })
    @GetMapping("/{participantId}/players")
    @ResponseBody
    public List<DraftResponse> getPlayersByParticipantId(
        @Parameter(description = "참여자 ID", required = true) @PathVariable("participantId") UUID participantId) {
        return draftService.getPlayersByParticipantId(participantId);
    }


    @Operation(summary = "드래프트 참여자 목록 조회", description = "드래프트 방에 속해있는 참여자 목록을 조회합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "참여자 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "드래프트를 찾을 수 없음")
    })
    @GetMapping("/{draftId}/participants")
    @ResponseBody
    public List<DraftParticipant> getParticipantsByDraftId(
        @Parameter(description = "드래프트 ID", required = true) @PathVariable("draftId") UUID draftId) {
        return draftService.getParticipantsByDraftId(draftId);
    }

    @Operation(summary = "드래프트 전체 선수 목록 조회", description = "드래프트에서 선택된 모든 선수 목록을 조회합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "선수 목록 조회 성공"),
        @ApiResponse(responseCode = "404", description = "드래프트를 찾을 수 없음")
    })
    @GetMapping("/{draftId}/allPlayers")
    @ResponseBody
    public List<DraftResponse> getAllPlayersByDraftId(
        @Parameter(description = "드래프트 ID", required = true) @PathVariable("draftId") UUID draftId) {
        return draftService.getAllPlayersByDraftId(draftId);
    }

    @Operation(summary = "스쿼드 제한 검사", description = "참여자의 스쿼드 제한을 검사합니다 (테스트용)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검사 완료"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping("squadLimit")
    @ResponseBody
    public boolean isWithinSquadLimits(
        @Parameter(description = "참여자 ID", required = true) @RequestParam(value="participantId") UUID participantId,
        @Parameter(description = "엘리먼트 타입 ID", required = true) @RequestParam(value="elementTypeId")UUID elementTypeId) {
        return draftService.isWithinSquadLimitsTest(participantId, elementTypeId);
    }
}
