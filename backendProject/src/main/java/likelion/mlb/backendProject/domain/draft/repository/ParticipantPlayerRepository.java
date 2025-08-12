package likelion.mlb.backendProject.domain.draft.repository;

import likelion.mlb.backendProject.domain.draft.entity.ParticipantPlayer;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.round.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantPlayerRepository extends JpaRepository<ParticipantPlayer, UUID> {

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
}
