package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.mlb.backendProject.domain.chat.dto.RosterResponse;
import likelion.mlb.backendProject.domain.chat.dto.ScoreboardItem;
import likelion.mlb.backendProject.domain.chat.entity.ChatRoom;
import likelion.mlb.backendProject.domain.chat.repository.ChatRoomRepository;
import likelion.mlb.backendProject.domain.chat.service.ChatRoomQueryService;

import likelion.mlb.backendProject.domain.draft.entity.ParticipantPlayer;
import likelion.mlb.backendProject.domain.draft.repository.DraftRepository;
import likelion.mlb.backendProject.domain.draft.repository.ParticipantPlayerRepository;

import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.match.repository.ParticipantRepository;

import likelion.mlb.backendProject.domain.round.entity.Round;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {

  private final ChatRoomRepository chatRoomRepository;
  private final DraftRepository draftRepository;
  private final ParticipantRepository participantRepository;
  private final ParticipantPlayerRepository participantPlayerRepository;

  /** roomId → draft/participants 로딩 */
  private Loaded load(UUID roomId) {
    ChatRoom room = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + roomId));

    UUID draftId = room.getDraftId();
    var draft = draftRepository.findById(draftId)
        .orElseThrow(() -> new IllegalArgumentException("Draft not found: " + draftId));

    List<Participant> participants = participantRepository.findByDraft(draft);
    return new Loaded(draftId, draft.getRound(), participants);
  }

  /** 방 스코어보드: 퍼포먼스 합계 기준 4명 랭킹 */
  @Override
  public List<ScoreboardItem> getScoreboard(UUID roomId) {
    var loaded = load(roomId);

    Map<UUID, Participant> pMap = loaded.participants.stream()
        .collect(Collectors.toMap(Participant::getId, p -> p));

    // round + participants 로 합산 점수 집계 (없으면 0점 처리)
    List<Object[]> rows = participantPlayerRepository
        .sumPointsByParticipant(loaded.round, loaded.participants);

    List<ScoreboardItem> items = new ArrayList<>();

    // 점수 있는 참가자
    for (Object[] r : rows) {
      UUID participantId = (UUID) r[0];
      long total = ((Number) r[1]).longValue();
      Participant p = pMap.get(participantId);

      String email = (p.isDummy() || p.getUser() == null) ? "BOT" : p.getUser().getEmail();
      UUID userId = (p.isDummy() || p.getUser() == null) ? null : p.getUser().getId();

      items.add(ScoreboardItem.builder()
          .participantId(participantId)
          .userId(userId)
          .email(email)
          .totalPoints(total)
          .rank(0)
          .build());
    }

    // 점수 레코드가 아직 없는 참가자도 0점으로 포함
    Set<UUID> present = items.stream().map(ScoreboardItem::getParticipantId).collect(Collectors.toSet());
    for (Participant p : loaded.participants) {
      if (!present.contains(p.getId())) {
        String email = (p.isDummy() || p.getUser() == null) ? "BOT" : p.getUser().getEmail();
        UUID userId = (p.isDummy() || p.getUser() == null) ? null : p.getUser().getId();

        items.add(ScoreboardItem.builder()
            .participantId(p.getId())
            .userId(userId)
            .email(email)
            .totalPoints(0L)
            .rank(0)
            .build());
      }
    }

    // 내림차순 정렬 + 랭크 부여(동점 처리 단순화)
    items.sort(Comparator.comparingLong(ScoreboardItem::getTotalPoints).reversed());
    int rank = 1;
    for (ScoreboardItem it : items) it.setRank(rank++);
    return items;
  }

  /** 특정 참가자의 로스터(11인) + 포메이션 문자열 */
  @Override
  public RosterResponse getRoster(UUID roomId, UUID participantId) {
    var loaded = load(roomId);

    // 해당 방 소속 참가자인지 검증
    boolean belongs = loaded.participants.stream().anyMatch(p -> p.getId().equals(participantId));
    if (!belongs) throw new IllegalArgumentException("Participant not in this room: " + participantId);

    List<ParticipantPlayer> picks = participantPlayerRepository.findByParticipant_Id(participantId);

    int gk=0, df=0, mid=0, fwd=0;
    List<RosterResponse.PlayerSlot> slots = new ArrayList<>();

    for (var pp : picks) {
      var pl = pp.getPlayer();
      int type = pl.getElementType().getFplId(); // 1=GK, 2=DEF, 3=MID, 4=FWD

      String pos = switch (type) {
        case 1 -> "GK";
        case 2 -> "DF";
        case 3 -> "MID";
        default -> "FWD";
      };

      if (type==1) gk++; else if (type==2) df++; else if (type==3) mid++; else fwd++;

      slots.add(RosterResponse.PlayerSlot.builder()
          .playerId(pl.getId())
          .name(pl.getWebName() != null ? pl.getWebName() : pl.getKrName()) // 이름 소스 통일
          .position(pos)
          .team(pl.getTeam().getName())  // 필요 시 getKrName()으로 교체 가능
          .build());
    }

    // 일반 표기: GK 제외 → DF-MID-FWD
    String formation = String.format("%d-%d-%d", df, mid, fwd);

    return RosterResponse.builder()
        .participantId(participantId)
        .formation(formation)
        .players(slots)
        .build();
  }

  private record Loaded(UUID draftId, Round round, List<Participant> participants) {}
}
