package likelion.mlb.backendProject.global.staticdata.repository;

import likelion.mlb.backendProject.global.staticdata.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RoundRepository extends JpaRepository<Round, UUID> {
}
