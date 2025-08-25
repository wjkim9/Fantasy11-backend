package likelion.mlb.backendProject.domain.player.repository;

import jakarta.transaction.Transactional;
import likelion.mlb.backendProject.domain.player.dto.PreviousBestPlayerDto;
import likelion.mlb.backendProject.domain.player.entity.Player;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
public interface PlayerRepository extends JpaRepository<Player, UUID> {


    List<Player> findAllByFplIdIn(List<Integer> codes);
    @Query("""
        SELECT p 
          FROM Player p
          JOIN FETCH p.team t
         WHERE p.fplId = :fplId
        """)
    Optional<Player> findByFplId(@Param("fplId") Integer fplId);

    @Modifying
    @Transactional
    @Query("UPDATE Player p SET p.status = 'x' WHERE p.fplId NOT IN :fplIds")
    void markDeletedByFplIdNotIn(@Param("fplIds") List<Integer> fplIds);



//    @Query("SELECT p FROM Player p WHERE p.fplId IN :ids")
//    List<Player> findAllByFplIdIn(@Param("ids") List<Integer> ids);

    default Map<Integer, Player> findAllByFplIdInAsMap(List<Integer> ids) {
        return findAllByFplIdIn(ids).stream()
                .collect(Collectors.toMap(Player::getFplId, Function.identity()));
    }

    @Query("""
          select p
            from Player p
           where p.elementType.fplId = :type
           and p.status = 'a'
        order by p.cost desc
        """)
    @EntityGraph(attributePaths = {
            "elementType",
            "team",
    })
    List<Player> findBestByType(@Param("type") int type, Pageable pageable);

    @Query("SELECT p FROM Player p " +"JOIN FETCH p.team t " +"JOIN FETCH p.elementType et")
    List<Player> findAllWithTeamAndElementType();


    @Query(value = """
        SELECT *
        FROM player p
        WHERE p.id NOT IN (
            SELECT pp.player_id
            FROM participant_player pp
            JOIN participant pa ON pp.participant_id = pa.id
            WHERE pa.draft_id = :draftId
        )
        AND p.element_type_id IN (:elementTypeIds)
        ORDER BY RANDOM()
        LIMIT 1
        """, nativeQuery = true)
    Optional<Player> findRandomAvailablePlayer(@Param("draftId") UUID draftId,
                                               @Param("elementTypeIds") List<UUID> elementTypeIds);

    @Query("""
        SELECT p 
        FROM Player p
        JOIN FETCH p.team
        JOIN FETCH p.elementType
        WHERE p.id = :playerId
    """)
    Optional<Player> findByIdWithTeamAndElementType(@Param("playerId") UUID playerId);
}
