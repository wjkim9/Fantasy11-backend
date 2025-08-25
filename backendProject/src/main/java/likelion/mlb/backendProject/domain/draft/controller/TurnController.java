package likelion.mlb.backendProject.domain.draft.controller;


import likelion.mlb.backendProject.domain.draft.dto.StartTurnRequest;
import likelion.mlb.backendProject.domain.draft.service.TurnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/draft")
@RequiredArgsConstructor
public class TurnController {

    private final TurnService turnService;

    /**
     * [POST] 현재 턴/라운드 설정 + 타이머 시작 + 즉시 브로드캐스트
     * body: { currentParticipantId, roundNo, pickWindowSec? }
     */
    @PostMapping("/{roomId}/turn")
    public ResponseEntity<Void> startOrUpdateTurn(
            @PathVariable UUID roomId,
            @RequestBody StartTurnRequest req
    ) {
        turnService.startOrUpdateTurn(roomId, req);
        return ResponseEntity.ok().build();
    }

    /**
     * [POST] 스냅샷 1회 재전송 (클라가 재동기화할 때 유용)
     */
    @PostMapping("/{roomId}/turn/snapshot")
    public ResponseEntity<Void> snapshot(@PathVariable UUID roomId) {
        turnService.broadcastTurnOnce(roomId);
        return ResponseEntity.ok().build();
    }
}