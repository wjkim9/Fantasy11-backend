package likelion.mlb.backendProject.domain.draft.repository;

import likelion.mlb.backendProject.domain.draft.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DraftRepository extends JpaRepository<Draft, UUID> {
}
