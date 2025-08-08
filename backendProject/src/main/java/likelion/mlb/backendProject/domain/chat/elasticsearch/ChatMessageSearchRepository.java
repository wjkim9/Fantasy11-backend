package likelion.mlb.backendProject.domain.chat.elasticsearch;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ChatMessageSearchRepository extends ElasticsearchRepository<ChatMessageDocument, String> {

  List<ChatMessageDocument> findByRoomIdOrderByCreatedAtAsc(String roomId);

  List<ChatMessageDocument> findByContentContaining(String keyword);
}
