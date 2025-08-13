package likelion.mlb.backendProject.domain.match.controller;

import likelion.mlb.backendProject.domain.match.dto.AssignDto;
import likelion.mlb.backendProject.domain.match.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/assignment")
    public ResponseEntity<AssignDto> getAssignment(
            //@AuthenticationPrincipal CustomUser principal // FIXME: 실제 타입으로

    ) {
        //UUID userId = principal.getId(); // 로그인 유저 PK
        UUID userId = UUID.fromString("969a6b7d-2a24-41ca-9f46-d1d2f8012844"); // FIXME 수정할 것
        AssignDto dto = assignmentService.getMyAssignmentOrThrow(userId); // 없으면 BaseException 던짐
        return ResponseEntity.ok(dto);
    }
}

