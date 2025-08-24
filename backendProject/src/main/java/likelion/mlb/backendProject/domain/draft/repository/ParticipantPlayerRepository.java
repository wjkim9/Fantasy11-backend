package likelion.mlb.backendProject.domain.draft.repository;

import java.util.List;
import likelion.mlb.backendProject.domain.draft.entity.ParticipantPlayer;

import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.round.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ParticipantPlayerRepository extends JpaRepository<ParticipantPlayer, UUID> {

  @Query("select distinct p.draft.id from ParticipantPlayer pp join pp.participant p join p.draft d where pp.player.id = :playerId and d.round.id = :roundId")
  List<UUID> findDraftIdsByPlayerAndRound(@Param("playerId") UUID playerId, @Param("roundId") UUID roundId);

    // 특정 드래프트 방에서 특정 선수가 이미 선택 되었는 지 체크
    boolean existsByParticipant_Draft_IdAndPlayer_Id(UUID draftId, UUID playerId);


    // Participant 엔티티로 드래프트한 선수들 조회
    List<ParticipantPlayer> findByParticipant(Participant participant);

    // Participant ID(UUID)로 드래프트한 선수들 조회
    List<ParticipantPlayer> findByParticipant_Id(UUID participantId);

    @Query("""
        select pp.participant.id, coalesce(sum(pfs.totalPoints), 0)
          from ParticipantPlayer pp
          left join PlayerFixtureStat pfs
                 on pfs.player = pp.player
                and pfs.fixture.round = :round
         where pp.participant in :participants
         group by pp.participant.id
    """)
    List<Object[]> sumPointsByParticipant(@Param("round") Round round,
                                          @Param("participants") List<Participant> participants);

    /*
    * 한 참가자가 선수를 드래프트 했을 시 포지션 별 최대/최소 값 유지하는 지 확인
    * */
    @Query("""
        SELECT CASE
            WHEN ((SELECT COUNT(pp)
                   FROM Player p
                   LEFT JOIN ParticipantPlayer pp
                     ON pp.player = p
                     AND pp.participant.id = :participantId
                   WHERE p.elementType.id = :elementTypeId) + 1) <= et.squadMaxPlay
            THEN true
            ELSE false
        END
        FROM ElementType et
        WHERE et.id = :elementTypeId
    """)
    Boolean isWithinSquadLimits(
            @Param("participantId") UUID participantId,
            @Param("elementTypeId") UUID elementTypeId
    );

    // draftId로 ParticipantPlayer 전부 가져오기
    List<ParticipantPlayer> findByParticipant_Draft_Id(UUID draftId);

  @Query("""
    select pp.participant.id as participantId,
           coalesce(sum(pfs.totalPoints), 0) as total
    from ParticipantPlayer pp
    left join PlayerFixtureStat pfs
           on pfs.player = pp.player
    left join Fixture fx on pfs.fixture = fx
    where pp.participant in :participants
      and fx.round = :round
      and (fx.started = true or fx.minutes > 0)
    group by pp.participant.id
  """)
  List<Object[]> sumLivePointsByParticipant(@Param("round") Round round,
      @Param("participants") List<Participant> participants);

  @Query("""
    select pp.participant.id as participantId,
           coalesce(sum(pfs.totalPoints), 0) as total
    from ParticipantPlayer pp
    left join PlayerFixtureStat pfs
           on pfs.player = pp.player
    left join Fixture fx on pfs.fixture = fx
    where pp.participant in :participants
      and fx.round = :round
      and fx.started = true and fx.finished = true
    group by pp.participant.id
  """)
  List<Object[]> sumFinishedPointsByParticipant(@Param("round") Round round,
      @Param("participants") List<Participant> participants);

  @Query("""
  select pp
    from ParticipantPlayer pp
    join fetch pp.player pl
    join fetch pl.team t
    join fetch pl.elementType et
   where pp.participant.id = :participantId
""")
  List<ParticipantPlayer> findByParticipantIdFetchAll(@Param("participantId") UUID participantId);

}
