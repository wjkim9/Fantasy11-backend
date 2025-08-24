package likelion.mlb.backendProject.domain.chat.service.Impl;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import likelion.mlb.backendProject.domain.player.entity.Player;
import likelion.mlb.backendProject.domain.player.repository.PlayerFixtureStatRepository;
import likelion.mlb.backendProject.domain.team.entity.Team;
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
  private final PlayerFixtureStatRepository playerFixtureStatRepository;

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

  @Override
  @Transactional(readOnly = true)
  public List<ScoreboardItem> getScoreboard(UUID roomId) {
    ChatRoom room = chatRoomRepository.findById(roomId)
        .orElseThrow(() -> new IllegalArgumentException("room not found: " + roomId));

    // draft + round + participants 로드 (fetch-join 가능한 기존 메서드 쓰면 더 좋아요)
    var draft = draftRepository.findById(room.getDraftId())
        .orElseThrow(() -> new IllegalArgumentException("draft not found: " + room.getDraftId()));
    Round round = draft.getRound();
    List<Participant> participants = draft.getParticipants();

    // ★ 라운드 내 진행/시작된 경기 기준 실시간 총점
    Map<UUID, Integer> totals = participantPlayerRepository
        .sumLivePointsByParticipant(round, participants)
        .stream()
        .collect(Collectors.toMap(
            r -> (UUID) r[0],
            r -> ((Number) r[1]).intValue()
        ));

    // 아이템 구성
    List<ScoreboardItem> items = new ArrayList<>();
    for (Participant p : participants) {
      int total = totals.getOrDefault(p.getId(), 0);

      items.add(ScoreboardItem.builder()
          .participantId(p.getId())
          .userId(p.isDummy() ? null : p.getUser().getId())
          .email(p.isDummy() ? "BOT" : p.getUser().getEmail())
          .totalPoints(total)                    // ★ 화면에 보여줄 점수
          .leaguePoints(p.getScore())            // 0~3 (정산 후에는 값 존재)
          .rank(0)                               // 아래서 계산해서 세팅
          .build());
    }

    // ★ 동점 동일 순위 부여(competition ranking: 1,1,3,4)
    items.sort(Comparator.comparingInt(ScoreboardItem::getTotalPoints).reversed());
    int shownRank = 0;
    Integer prev = null;
    for (int i = 0; i < items.size(); i++) {
      int tp = items.get(i).getTotalPoints();
      if (prev == null || !prev.equals(tp)) shownRank = i + 1;
      items.get(i).setRank(shownRank);
      prev = tp;
    }

    return items;
  }


  @Transactional(readOnly = true)
  public RosterResponse getRoster(UUID roomId, UUID participantId) {
    // 1) 참가자
    Participant participant = participantRepository.findById(participantId)
        .orElseThrow(() -> new IllegalArgumentException("participant not found"));

    Round round = participant.getDraft().getRound();

    // 2) 참가자 로스터(선택 선수들) 한번에 로드
    List<ParticipantPlayer> picks =
        participantPlayerRepository.findByParticipantIdFetchAll(participantId);

    // 선수 UUID 목록
    List<UUID> playerIds = picks.stream()
        .map(pp -> pp.getPlayer().getId())
        .toList();

    // 3) 선수별 라운드 포인트 합계 맵
    Map<UUID, Integer> ptsByPlayer = Map.of();
    if (!playerIds.isEmpty()) {
      List<Object[]> ptsRows = playerFixtureStatRepository
          .sumPlayerPointsForRound(round, playerIds);

      ptsByPlayer = ptsRows.stream().collect(Collectors.toMap(
          row -> (UUID) row[0],
          row -> ((Number) row[1]).intValue()
      ));
    }

    // 4) 포지션 매핑: fplId → GK/DF/MID/FWD
    Function<Player, String> toPos = pl -> {
      int fplPos = pl.getElementType().getFplId(); // 1=GK,2=DEF,3=MID,4=FWD
      return switch (fplPos) {
        case 1 -> "GK";
        case 2 -> "DF";
        case 3 -> "MID";
        case 4 -> "FWD";
        default -> "MID";
      };
    };

    // 팀 이름(한글 우선)
    Function<Team, String> teamName = t -> {
      String kr = t.getKrName();
      return (kr != null && !kr.isBlank()) ? kr : t.getName();
    };

    // 표시 이름: krName 우선, 없으면 webName
    Function<Player, String> displayName = pl -> {
      String kr = pl.getKrName();
      return (kr != null && !kr.isBlank()) ? kr : pl.getWebName();
    };

    // 5) 카드 생성 + 포메이션 카운트
    int df = 0, mid = 0, fwd = 0; // GK는 포메이션에서 제외
    List<RosterResponse.PlayerSlot> slots = new ArrayList<>();

    for (ParticipantPlayer pp : picks) {
      Player pl = pp.getPlayer();
      String pos = toPos.apply(pl);

      if ("DF".equals(pos)) df++;
      else if ("MID".equals(pos)) mid++;
      else if ("FWD".equals(pos)) fwd++;

      slots.add(RosterResponse.PlayerSlot.builder()
          .playerId(pl.getId())
          .name(displayName.apply(pl))
          .position(pos)
          .team(teamName.apply(pl.getTeam()))
          .points(ptsByPlayer.getOrDefault(pl.getId(), 0))
          .pic(pl.getPic())
          .build());
    }

    // 6) 포메이션 표기: GK 제외 → DF-MID-FWD
    String formation = String.format("%d-%d-%d", df, mid, fwd);

    return RosterResponse.builder()
        .participantId(participant.getId())
        .formation(formation)
        .players(slots)
        .build();
  }



  private record Loaded(UUID draftId, Round round, List<Participant> participants) {}
}
