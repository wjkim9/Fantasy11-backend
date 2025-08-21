package likelion.mlb.backendProject.global.configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

  @Value("${app.elasticsearch.url:http://localhost:9200}")
  private String esUrl;

  @Value("${app.elasticsearch.username:}")   // xpack.security.enabled=false면 비워둠
  private String username;

  @Value("${app.elasticsearch.password:}")
  private String password;

  @Bean(destroyMethod = "close")
  public RestClient restClient() {
    var builder = RestClient.builder(HttpHost.create(esUrl))
        .setCompressionEnabled(true)
        .setRequestConfigCallback(rcb -> rcb
            .setConnectTimeout(5_000)   // TCP 연결 타임아웃
            .setSocketTimeout(60_000)); // 응답 대기 타임아웃

    // 보안 켜질 경우만 적용 (지금은 xpack.security.enabled=false라 스킵됨)
    if (!username.isBlank()) {
      CredentialsProvider creds = new BasicCredentialsProvider();
      creds.setCredentials(AuthScope.ANY,
          new UsernamePasswordCredentials(username, password));
      builder.setHttpClientConfigCallback(hcb -> hcb.setDefaultCredentialsProvider(creds));
    }

    return builder.build();
  }

  @Bean(destroyMethod = "close")
  public RestClientTransport restClientTransport(RestClient restClient, ObjectMapper springMapper) {
    // Spring의 ObjectMapper를 써서 Instant 같은 JavaTime 직렬화가 맞게 동작
    return new RestClientTransport(restClient, new JacksonJsonpMapper(springMapper));
  }

  @Bean
  public ElasticsearchClient elasticsearchClient(RestClientTransport transport) {
    return new ElasticsearchClient(transport);
  }
}
