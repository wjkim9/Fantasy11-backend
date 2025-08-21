package likelion.mlb.backendProject.domain.user.service.Impl;

import java.util.Optional;
import java.util.UUID;

import likelion.mlb.backendProject.domain.chat.service.ChatRoomService;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;
import likelion.mlb.backendProject.domain.user.service.UserRoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoomQueryServiceImpl implements UserRoomQueryService {

  private final ParticipantRepository participantRepository;
  private final ChatRoomService chatRoomService;

  @Override
  public Optional<UUID> findLatestChatRoomForUser(UUID userId) {
    var list = participantRepository.findLatestByUser(userId, PageRequest.of(0, 1));
    if (list.isEmpty()) return Optional.empty();

    Participant p = list.get(0);
    UUID draftId = p.getDraft().getId();

    // 채팅방이 없다면 생성 후 반환(안전망)
    try {
      return Optional.of(chatRoomService.getRoomIdByDraft(draftId));
    } catch (IllegalArgumentException e) {
      return Optional.of(chatRoomService.createForDraft(draftId).getId());
    }
  }
}