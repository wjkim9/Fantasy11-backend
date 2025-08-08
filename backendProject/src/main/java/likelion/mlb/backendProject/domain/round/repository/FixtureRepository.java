package likelion.mlb.backendProject.domain.round.repository;

import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.domain.round.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, UUID> {

    List<Fixture> findByRound(Round round);

    Optional<Fixture> findByFplId(Integer fplId);


    @Query("SELECT f FROM Fixture f WHERE f.fplId IN :ids")
    List<Fixture> findAllByFplIdIn(@Param("ids") List<Integer> ids);

    default Map<Integer, Fixture> findAllByFplIdInAsMap(List<Integer> ids) {
        return findAllByFplIdIn(ids).stream()
                .collect(Collectors.toMap(Fixture::getFplId, Function.identity()));
    }

    @Query("""
    SELECT f
      FROM Fixture f
      JOIN FETCH f.round r
     WHERE f.started = true
       AND f.finished = false
    """)
    List<Fixture> findByStartedTrueAndFinishedFalse();


    Optional<List<Fixture>> findByRoundIsPreviousTrue();
}
