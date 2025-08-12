package likelion.mlb.backendProject.domain.round.service;

import likelion.mlb.backendProject.domain.round.repository.RoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoundService {
    private final RoundRepository roundRepository;
}
