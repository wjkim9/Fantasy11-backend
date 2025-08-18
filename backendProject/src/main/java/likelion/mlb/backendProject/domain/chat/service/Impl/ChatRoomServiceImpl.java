package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomService;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomServiceImpl implements ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;

  @Override
  public ChatRoom createForDraft(UUID draftId) {
    return chatRoomRepository.findByDraftId(draftId)
        .orElseGet(() -> chatRoomRepository.save(
            ChatRoom.builder().draftId(draftId).build()
        ));
  }

  @Override
  @Transactional(readOnly = true)
  public UUID getRoomIdByDraft(UUID draftId) {
    return chatRoomRepository.findByDraftId(draftId)
        .map(ChatRoom::getId)
        .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found for draft " + draftId));
  }
}
