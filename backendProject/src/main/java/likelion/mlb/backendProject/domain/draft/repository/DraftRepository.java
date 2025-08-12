package likelion.mlb.backendProject.domain.draft.repository;

import java.util.List;
import java.util.Optional;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.round.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DraftRepository extends JpaRepository<Draft, UUID> {

  Optional<Draft> findByRoundId(UUID roundId);

  @Query("""
        select d from Draft d
        left join fetch d.participants p
        left join fetch p.user
        where d.round = :round
    """)
  List<Draft> findByRoundFetchParticipants(@Param("round") Round round);
}
