package likelion.mlb.backendProject.domain.player.repository;

import likelion.mlb.backendProject.domain.player.entity.live.PlayerFixtureStat;
import likelion.mlb.backendProject.domain.round.entity.Fixture;
import likelion.mlb.backendProject.domain.round.entity.Round;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerFixtureStatRepository extends JpaRepository<PlayerFixtureStat, UUID> {
    // 구현 예시
    @Query("SELECT pfs FROM PlayerFixtureStat pfs WHERE pfs.fixture.fplId IN :fixtureFplIds")
    List<PlayerFixtureStat> findByFixtureFplIds(@Param("fixtureFplIds") List<Integer> fixtureFplIds);


    /**
     * fixtures 에 속하면서 inDreamteam=true 인 Stat 들을
     * player → team, elementType 까지 함께 로드
     */
    @EntityGraph(attributePaths = {
            "player",
            "player.team",
            "player.elementType"
    })
    List<PlayerFixtureStat> findByFixtureInAndInDreamteamTrue(
            List<Fixture> fixtures);
    @Query("""
  select p.id as playerId, coalesce(sum(pfs.totalPoints), 0) as pts
  from PlayerFixtureStat pfs
  join pfs.player p
  join pfs.fixture f
  where f.round = :round
    and p.id in :playerIds
  group by p.id
""")
    List<Object[]> sumPlayerPointsForRound(@Param("round") Round round,
        @Param("playerIds") List<UUID> playerIds);

}