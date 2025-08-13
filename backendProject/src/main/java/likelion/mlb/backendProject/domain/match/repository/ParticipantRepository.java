package likelion.mlb.backendProject.domain.match.repository;

import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
  boolean existsByDraft_IdAndUser_Id(UUID draftId, UUID userId);

  Optional<Participant> findByUserAndDraft(User user, Draft draft);
}
