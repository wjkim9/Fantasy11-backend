package likelion.mlb.backendProject.global.staticdata.repository;

import likelion.mlb.backendProject.global.staticdata.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface RoundRepository extends JpaRepository<Round, UUID> {
    Round findFirstByStartedAtAfterOrderByStartedAtAsc(OffsetDateTime startedAt);

}