package likelion.mlb.backendProject.domain.player.elasticsearch.repository;

import likelion.mlb.backendProject.domain.player.elasticsearch.dto.PlayerEsDocument;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerEsRepository extends ElasticsearchRepository<PlayerEsDocument, String> {

    // 문서 ID로 데이터 삭제하는 쿼리 메서드
    void deleteById(String id);

}
