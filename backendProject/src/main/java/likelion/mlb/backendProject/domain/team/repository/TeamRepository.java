package likelion.mlb.backendProject.domain.team.repository;

import likelion.mlb.backendProject.domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByFplId(Integer fplId);

    List<Team> findAllByFplIdIn(List<Integer> fplIds);


    // position 오름차순 정렬
    List<Team> findAllByOrderByPositionAsc();
}
