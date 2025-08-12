package likelion.mlb.backendProject.domain.round.repository;

import likelion.mlb.backendProject.domain.round.entity.RoundScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoundScoreRepository extends JpaRepository<RoundScore, UUID> {

    boolean existsByUserIdAndRoundId(UUID userId, UUID roundId);

}
