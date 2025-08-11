package likelion.mlb.backendProject.domain.draft.repository;

import java.util.Optional;
import likelion.mlb.backendProject.domain.draft.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DraftRepository extends JpaRepository<Draft, UUID> {

  Optional<Draft> findByRoundId(UUID roundId);
}
