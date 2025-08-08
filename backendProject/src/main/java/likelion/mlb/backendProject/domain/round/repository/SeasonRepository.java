package likelion.mlb.backendProject.domain.round.repository;

import likelion.mlb.backendProject.domain.round.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SeasonRepository extends JpaRepository<Season, UUID> {
    Season findBySeasonName(String seasonName);
}
