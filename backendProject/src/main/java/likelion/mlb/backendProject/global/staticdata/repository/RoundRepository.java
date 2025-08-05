package likelion.mlb.backendProject.global.staticdata.repository;

import likelion.mlb.backendProject.global.staticdata.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RoundRepository extends JpaRepository<Round,UUID> {
    Round findFirstByStartedAtAfterOrderByStartedAtAsc(LocalDateTime now);
}
