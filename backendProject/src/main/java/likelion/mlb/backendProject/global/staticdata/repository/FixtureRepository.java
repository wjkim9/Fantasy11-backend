package likelion.mlb.backendProject.global.staticdata.repository;

import likelion.mlb.backendProject.global.staticdata.entity.Fixture;
import likelion.mlb.backendProject.global.staticdata.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FixtureRepository extends JpaRepository<Fixture, UUID> {

    List<Fixture> findByRound(Round round);
}
