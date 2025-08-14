package likelion.mlb.backendProject.domain.match.service;

import likelion.mlb.backendProject.domain.match.dto.AssignDto;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final ParticipantRepository participantRepository;
    private final DraftTimingService draftTimingService;

    @Transactional(readOnly = true)
    public AssignDto getMyAssignmentOrThrow(UUID userId) {
        var roundInfo = draftTimingService.getNextDraftWindowOrThrow();
        var roundId = roundInfo.getId();
        try {
            AssignDto dto = participantRepository.findAssignment(userId, roundId);
            if (dto == null) {
                throw new BaseException(ErrorCode.ASSIGNMENT_NOT_FOUND); // 404 등
            }
            return dto;
        } catch (IncorrectResultSizeDataAccessException e) {
            // 데이터 이상(동시에 두 드래프트에 배정 등)
            throw new BaseException(ErrorCode.ASSIGNMENT_CONFLICT); // 409 등
        }
    }
}

