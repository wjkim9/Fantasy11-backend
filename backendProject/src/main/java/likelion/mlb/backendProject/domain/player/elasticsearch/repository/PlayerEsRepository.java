package likelion.mlb.backendProject.domain.player.elasticsearch.repository;

import likelion.mlb.backendProject.domain.player.elasticsearch.dto.PlayerEsDocument;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerEsRepository extends ElasticsearchRepository<PlayerEsDocument, String> {

}
