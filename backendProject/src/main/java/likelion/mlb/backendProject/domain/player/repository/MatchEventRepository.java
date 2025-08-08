package likelion.mlb.backendProject.domain.player.repository;

import likelion.mlb.backendProject.domain.player.entity.live.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchEventRepository extends JpaRepository<MatchEvent, UUID> {
}
