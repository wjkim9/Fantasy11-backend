package likelion.mlb.backendProject.domain.round.repository;

import likelion.mlb.backendProject.domain.round.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {

    Optional<Round> findByRound(Integer round);

    Optional<Round> findByIsCurrentTrue();

    Optional<Round> findByIsPreviousTrue();


    List<Round> findAllByRoundIn(List<Integer> rounds);

    Round findFirstByStartedAtAfterOrderByStartedAtAsc(OffsetDateTime nowUTC);


    @Query("""
       select r
         from Round r
        where r.endedAt is not null
          and r.finished = true
          and r.settled = false
    """)
    List<Round> findUnsettledFinishedRounds();


}
