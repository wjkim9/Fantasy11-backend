package likelion.mlb.backendProject.domain.player.repository;

import likelion.mlb.backendProject.domain.player.entity.ElementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ElementTypeRepository extends JpaRepository<ElementType, UUID> {
}
