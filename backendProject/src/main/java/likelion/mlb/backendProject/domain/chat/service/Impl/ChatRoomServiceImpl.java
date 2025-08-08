package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.time.LocalDateTime;
import java.util.UUID;
import likelion.mlb.backendProject.domain.chat.dto.ChatRoomDto;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomService;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.global.exception.BaseException;
import likelion.mlb.backendProject.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
  private final ChatRoomRepository roomRepo;
  private final DraftRepository draftRepo;

  @Override
  public ChatRoomDto createRoom(UUID draftId) {
    // 1) 해당 draft 존재 확인
    Draft draft = draftRepo.findById(draftId)
        .orElseThrow(() -> new BaseException(ErrorCode.INVALID_INPUT_VALUE));

    // 2) 이미 방이 생성돼 있으면(옵션) 재반환하거나 예외
    roomRepo.findByDraft(draft).ifPresent(room -> {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    });

    // 3) 방 생성
    ChatRoom room = new ChatRoom();
    room.setId(UUID.randomUUID());
    room.setDraft(draft);
    room.setCreatedAt(LocalDateTime.now());
    roomRepo.save(room);

    // 4) DTO 반환
    return new ChatRoomDto(
        room.getId(),
        draft.getId(),
        room.getCreatedAt()
    );
  }

}
