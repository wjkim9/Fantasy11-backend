package likelion.mlb.backendProject.domain.player.elasticsearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import likelion.mlb.backendProject.domain.player.elasticsearch.dto.PlayerEsDocument;
import likelion.mlb.backendProject.domain.player.elasticsearch.repository.PlayerEsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerEsService {

    // 엘라스틱서치에 명령을 전달하는 자바 API
    private final ElasticsearchClient elasticsearchClient;

    private final PlayerEsRepository playerEsRepository;

    // 단건 데이터 저장 메서드
    public void save(PlayerEsDocument document) {
        playerEsRepository.save(document);
    }

    // 복수 데이터 저장 메서드
    public void saveAll(List<PlayerEsDocument> documentList) {
        playerEsRepository.saveAll(documentList);
    }

    // 데이터 삭제 메서드
    public void deleteById(String id) {
        playerEsRepository.deleteById(id);
    }

    // 검색 키워드를 받아서 엘라스틱서치에서 검색하는 메서드
    public List<PlayerEsDocument> search(String keyword, String elementTypeId) {

        try {
            // 엘라스틱서치에서 사용할 검색 조건을 담는 객체
            Query query;

            // 포지션과 검색어가 없으면 모든 문서를 검색하는 matchAll쿼리
            if ((keyword == null || keyword.isBlank()) && (elementTypeId == null || elementTypeId.isBlank())) {
                query = MatchAllQuery.of(m->m)._toQuery(); // 전체 문서를 가져오는 쿼리를 생성하는 람다 함수
            }
            // 검색어가 있을 때
            else {
                // BoolQuery는 복수 조건을 조합할 때 사용하는 쿼리
                // 이 쿼리 안에서 여러 개의 조건을 나열
                query = BoolQuery.of(b ->{

                    // keyword 조건이 있으면 should
                    if (keyword != null && !keyword.isBlank()) {
                        b.should(MatchQuery.of(m -> m.field("webName.ngram").query(keyword))._toQuery());
                        b.should(MatchQuery.of(m -> m.field("krName.ngram").query(keyword))._toQuery());
                        b.minimumShouldMatch("1"); // keyword 조건 중 최소 1개는 만족해야 함
                    }

                    // elementTypeId 조건이 있으면 must
                    if (elementTypeId != null && !elementTypeId.isBlank()) {
                        b.must(TermQuery.of(t -> t.field("elementTypeId.keyword").value(elementTypeId))._toQuery());
                    }

                    return b;
                })._toQuery();
            }
            // searchRequest는 엘라스틱서치에서 검색을 하기 위한 검색요청 객체
            // 인덱스명, 쿼리를 포함한 검색 요청
            SearchRequest request = SearchRequest.of(s->s
                .index("player-index")

                // 한 번에 최대 700개 데이터 가져올 수 있음
                // 화면에서 페이징을 안 쓰기 때문에 scroll혹은 search_after방식은 적합 X
                .size(700)
                .query(query)
            );

            // SearchResponse는 엘라스틱서치의 검색 결과를 담고 있는 응답 객체
            SearchResponse<PlayerEsDocument> response =
                    // 엘라스틱서치에 명령을 전달하는 자바 API검색 요청을 담아서 응답 객체로 반환
                    elasticsearchClient.search(request, PlayerEsDocument.class);

            // 위 응답객체에서 받은 검색 결과 중 문서만 추출해서 리스트로 만듬
            // hit는 엘라스틱서치에서 검색 된 문서 1개를 감싸고 있는 객체
            List<PlayerEsDocument> content = response.hits() // 엘라스틱 서치 응답에서 hits(문서 검색결과) 전체를 꺼냄
                    .hits() // 검색 결과 안에 개별 리스트를 가져옴
                    .stream() // 자바 stream api를 사용
                    .map(Hit::source) // 각 Hit 객체에서 실제 문서를 꺼내는 작업
                    .collect(Collectors.toList()); // 위에서 꺼낸 객체를 자바 List에 넣음

            return content;
        } catch (IOException ioException) {
            log.error("검색 오류 ", ioException.getMessage());
            throw new RuntimeException("검색 오류 중 발생 ", ioException);
        }

    }

    // 선수 포지션 pk값 받아서 엘라스틱서치에서 검색하는 메서드
    /*
    public List<PlayerEsDocument> searchWithElementTypeId(String elementTypeId) {

        try {
            // 엘라스틱서치에서 사용할 검색 조건을 담는 객체
            Query query;

            // 검색어가 없으면 모든 문서를 검색하는 matchAll쿼리
            if (elementTypeId == null || elementTypeId.isBlank()) {
                query = MatchAllQuery.of(m->m)._toQuery(); // 전체 문서를 가져오는 쿼리를 생성하는 람다 함수
            }
            // 검색어가 있을 때
            else {
                // BoolQuery는 복수 조건을 조합할 때 사용하는 쿼리
                // 이 쿼리 안에서 여러 개의 조건을 나열
                query = BoolQuery.of(b ->{

                    // MatchQuery는 해당 단어가 포함되어있는 지 검사하는 쿼리
                    b.should(MatchQuery.of(m->m.field("elementTypeId.ngram").query(elementTypeId))._toQuery());

                    return b;
                })._toQuery();
            }
            // searchRequest는 엘라스틱서치에서 검색을 하기 위한 검색요청 객체
            // 인덱스명, 쿼리를 포함한 검색 요청
            SearchRequest request = SearchRequest.of(s->s
                    .index("player-index")
                    .query(query)
            );

            // SearchResponse는 엘라스틱서치의 검색 결과를 담고 있는 응답 객체
            SearchResponse<PlayerEsDocument> response =
                    // 엘라스틱서치에 명령을 전달하는 자바 API검색 요청을 담아서 응답 객체로 반환
                    elasticsearchClient.search(request, PlayerEsDocument.class);

            // 위 응답객체에서 받은 검색 결과 중 문서만 추출해서 리스트로 만듬
            // hit는 엘라스틱서치에서 검색 된 문서 1개를 감싸고 있는 객체
            List<PlayerEsDocument> content = response.hits() // 엘라스틱 서치 응답에서 hits(문서 검색결과) 전체를 꺼냄
                    .hits() // 검색 결과 안에 개별 리스트를 가져옴
                    .stream() // 자바 stream api를 사용
                    .map(Hit::source) // 각 Hit 객체에서 실제 문서를 꺼내는 작업
                    .collect(Collectors.toList()); // 위에서 꺼낸 객체를 자바 List에 넣음

            return content;
        } catch (IOException ioException) {
            log.error("검색 오류 ", ioException.getMessage());
            throw new RuntimeException("검색 오류 중 발생 ", ioException);
        }

    }
    */
    
}
