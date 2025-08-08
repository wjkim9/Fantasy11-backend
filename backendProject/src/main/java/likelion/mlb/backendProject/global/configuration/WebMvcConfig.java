package likelion.mlb.backendProject.global.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${frontend.http.url}")
    private String httpUrl;

    @Value("${frontend.https.url}")
    private String httpsUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(httpUrl, httpsUrl)
                .allowedMethods("*")
                .allowedHeaders("Content-Type", "Accept", "X-Requested-With", "Authorization")
                .allowCredentials(true); // 이 부분이 중요합니다.
    }
}
