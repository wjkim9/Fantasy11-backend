package likelion.mlb.backendProject.domain.chat.elasticsearch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@Document(indexName = "chat-messages")
@Setting(settingPath = "/elasticsearch/chat-messages/settings.json")
@Mapping(mappingPath = "/elasticsearch/chat-messages/mappings.json")
public class ChatMessageDocument {

  @Id
  private String id;

  @Field(type = FieldType.Keyword)
  private String roomId;

  @Field(type = FieldType.Keyword)
  private String userId;

  @Field(type = FieldType.Text,
      analyzer = "ngram_analyzer",
      searchAnalyzer = "ngram_analyzer")
  private String content;

  @Field(type = FieldType.Keyword)
  private String messageType;

  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
  private LocalDateTime createdAt;
}