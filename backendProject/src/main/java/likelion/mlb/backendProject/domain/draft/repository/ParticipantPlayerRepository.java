package likelion.mlb.backendProject.domain.draft.repository;

import likelion.mlb.backendProject.domain.draft.entity.ParticipantPlayer;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParticipantPlayerRepository extends JpaRepository<ParticipantPlayer, UUID> {
}
