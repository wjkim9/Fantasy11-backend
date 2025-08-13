package likelion.mlb.backendProject.domain.user.repository;

import likelion.mlb.backendProject.domain.user.entity.SeasonUserScore;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonUserScoreRepository extends JpaRepository<SeasonUserScore, UUID> {
    Optional<SeasonUserScore> findBySeasonIdAndUserId(UUID seasonId, UUID userId);


    @Query("""
        select s
        from SeasonUserScore s
        join fetch s.user u
        join s.season se
        where se.seasonName = :seasonName
        order by s.score desc, s.points desc, u.id
    """)
    List<SeasonUserScore> findBySeasonBestUsers(
            @Param("seasonName") String seasonName,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SeasonUserScore s
           set s.score  = s.score  + :scoreDelta,
               s.points = s.points + :pointsDelta
         where s.season.id = :seasonId
           and s.user.id   = :userId
    """)
    int increment(@Param("seasonId") UUID seasonId,
                  @Param("userId")   UUID userId,
                  @Param("scoreDelta")  int scoreDelta,
                  @Param("pointsDelta") int pointsDelta);
}
