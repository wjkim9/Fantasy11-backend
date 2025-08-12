package likelion.mlb.backendProject.global.security.oauth;

import java.util.List;
import java.util.Optional;
import likelion.mlb.backendProject.domain.user.entity.User;
import likelion.mlb.backendProject.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = new DefaultOAuth2UserService().loadUser(request);

        String email = user.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Google 응답에 email 없음");
        }

        String name = user.getAttribute("name") != null ? user.getAttribute("name") : "unnamed";

        User saved;

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            saved = optionalUser.get();
        } else {
            User newUser = User.builder()
                .email(email)
                .name(name)
                .build();
            saved = userRepository.save(newUser);
        }

        return new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_" + saved.getRole())),
            user.getAttributes(),
            "email"
        );
    }

}