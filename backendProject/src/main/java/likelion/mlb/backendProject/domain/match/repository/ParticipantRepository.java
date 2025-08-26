package likelion.mlb.backendProject.domain.match.repository;


import likelion.mlb.backendProject.domain.match.dto.AssignDto;

import likelion.mlb.backendProject.domain.draft.entity.Draft;
import likelion.mlb.backendProject.domain.match.entity.Participant;
import likelion.mlb.backendProject.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    boolean existsByDraft_IdAndUser_Id(UUID draftId, UUID userId);


    @Query("""
       select new likelion.mlb.backendProject.domain.match.dto.AssignDto(p.draft.id, p.userNumber)
       from Participant p
       where p.dummy = false
         and p.user.id = :userId
         and p.draft.isDeleted = false
         and p.draft.round.id = :roundId
    """)
    AssignDto findAssignment(@Param("userId") UUID userId,
                             @Param("roundId") UUID roundId);

    Optional<Participant> findByUserAndDraft(User user, Draft draft);

    List<Participant> findByDraft(Draft draft);

    @Query("""
    select p
      from Participant p
      join p.draft d
      join d.round r
     where p.dummy = false
       and p.user.id = :userId
       and d.isDeleted = false
  order by r.startedAt desc
""")
    java.util.List<likelion.mlb.backendProject.domain.match.entity.Participant>
    findLatestByUser(@Param("userId") java.util.UUID userId,
        org.springframework.data.domain.Pageable pageable);


    Optional<Participant> findByDraftAndUserNumber(Draft draft, short userNumber);
}
