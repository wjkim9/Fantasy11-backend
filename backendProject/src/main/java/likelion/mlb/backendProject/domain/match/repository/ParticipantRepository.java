package likelion.mlb.backendProject.domain.match.repository;

import likelion.mlb.backendProject.domain.match.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
  boolean existsByDraft_IdAndUser_Id(UUID draftId, UUID userId);
}
