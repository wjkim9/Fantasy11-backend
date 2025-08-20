package likelion.mlb.backendProject.global.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

  @Bean
  public RestClient restClient() {
    return RestClient.builder(HttpHost.create("http://localhost:9200")).build();
  }

  @Bean
  public ElasticsearchClient elasticsearchClient(RestClient restClient, ObjectMapper springMapper) {
    var mapper = new JacksonJsonpMapper(springMapper);
    var transport = new RestClientTransport(restClient, mapper);
    return new ElasticsearchClient(transport);
  }
}

